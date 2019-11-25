package bo.tc.tcplanner.app;

import bo.tc.tcplanner.datastructure.LocationHierarchyMap;
import bo.tc.tcplanner.datastructure.TimelineBlock;
import bo.tc.tcplanner.datastructure.ValueEntryMap;
import bo.tc.tcplanner.domain.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.jakewharton.fliptables.FlipTable;
import org.apache.commons.io.IOUtils;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.api.solver.event.SolverEventListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static bo.tc.tcplanner.app.TCSchedulingApp.*;
import static bo.tc.tcplanner.app.Toolbox.*;
import static bo.tc.tcplanner.domain.DataStructureBuilder.dummyExecutionMode;

public class SolverThread extends Thread {
    JsonServer jsonServer;
    boolean continuetosolve = true;
    String P1_mode = "incremental";
    String P2_mode = "global";
    String solvingStatus;

    public Solver<Schedule> getCurrentSolver() {
        return currentSolver;
    }

    public void setCurrentSolver(Solver<Schedule> currentSolver) {
        this.currentSolver = currentSolver;
    }

    Solver<Schedule> currentSolver;
    Schedule currentSchedule;
    private List<Solver<Schedule>> solverList;
    private Object resumeSolvingLock;
    private Object newTimelineBlockLock;

    @Override
    public void run() {
        while (true) {
            //Wait
            synchronized (resumeSolvingLock) {
                try {
                    resumeSolvingLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            displayTray("Solving Started", "Good Luck");
            try {
                initializeFiles();
                initializeSolvers();
                runSolve();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void initializeFiles() {
        // Load TimelineBlock & ValueEntryMap
        try {
            valueEntryMap = new ObjectMapper().readValue(
                    IOUtils.toString(new FileInputStream(new File(fpath_ValueEntryMap)), StandardCharsets.UTF_8), ValueEntryMap.class);
            locationHierarchyMap = new ObjectMapper().readValue(
                    IOUtils.toString(new FileInputStream(new File(fpath_LocationHierarchyMap)), StandardCharsets.UTF_8),
                    LocationHierarchyMap.class);
            timeHierarchyMap = (HashMap<String, Object>) (new ObjectMapper().readValue("{\"root\":"
                    + IOUtils.toString(new FileInputStream(new File(fpath_TimeHierarchyMap)), StandardCharsets.UTF_8) + "}", Map.class)
                    .get("root"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void initializeSolvers() {
        SolverFactory<Schedule> solverFactory;
        solverList = new ArrayList<>();

        // Solve Phase 1 : to met requirements
        solverFactory = SolverFactory.createFromXmlInputStream(this.getClass().getResourceAsStream("/solverPhase1.xml"));
        Solver solver1 = solverFactory.buildSolver();
        setSolverListener(solver1);
        solverList.add(solver1);

        // Solve Phase 2 : to remove dummyjobs
        solverFactory = SolverFactory.createFromXmlInputStream(this.getClass().getResourceAsStream("/solverPhase2.xml"));
        Solver solver2 = solverFactory.buildSolver();
        setSolverListener(solver2);
        solverList.add(solver2);
    }

    public void setSolverListener(Solver<Schedule> solver) {
        solver.addEventListener(bestSolutionChangedEvent -> {
            printCurrentSolution(bestSolutionChangedEvent.getNewBestSolution(), solver, false, solvingStatus);
            currentSchedule = bestSolutionChangedEvent.getNewBestSolution();
            jsonServer.updateTimelineBlock(false, bestSolutionChangedEvent.getNewBestSolution());
        });
    }

    public Schedule getBestSolution() {
        for (Solver solver : Lists.reverse(solverList)) {
            if (solver.getBestSolution() != null) return (Schedule) solver.getBestSolution();
        }
        return null;
    }

    public void terminateSolver() {
        setContinuetosolve(false);
        if (solverList != null) for (Solver solver : solverList) solver.terminateEarly();
    }

    public void restartSolversWithNewTimelineBlock(TimelineBlock timelineBlock) {
        terminateSolver();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        jsonServer.setLatestTimelineBlock(timelineBlock);
        synchronized (resumeSolvingLock) {
            resumeSolvingLock.notify();
        }
    }

    public void restartSolvers() {
        terminateSolver();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        synchronized (resumeSolvingLock) {
            resumeSolvingLock.notify();
        }
    }

    public static DataStructureBuilder initializeData(TimelineBlock latestTimelineBlock) throws IOException {
        // Build DataStructure
        DataStructureBuilder DSB = new DataStructureBuilder();
        DSB.setGlobalProperties(latestTimelineBlock);
        DSB.addResourcesFromValueEntryMap(valueEntryMap, DSB.getDefaultProject());
        DSB.addJobsFromValueEntryDict(valueEntryMap, DSB.getDefaultProject());
        DSB.addJobsFromTimelineBlock(latestTimelineBlock, DSB.getDefaultProject());
        DSB.initializeAllocationList(20);
        DSB.initializeSchedule();
        DataStructureBuilder.constructChainProperty(DSB.getListOfAllocations());
        return DSB;
    }

    public Schedule runSolve() throws IOException {
        continuetosolve = true;
        DataStructureBuilder DSB = null;
        Schedule result = null;
        try {
            DSB = initializeData(jsonServer.getLatestTimelineBlock());
            result = DSB.getFullSchedule();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Solve Hard Incremental By AllocationList
        if (P1_mode.equals("incremental")) {
            currentSolver = solverList.get(0);
            List<Allocation> fullAllocationList = new ArrayList<>(result.getAllocationList());
            result.setAllocationList(new ArrayList<>(Arrays.asList(fullAllocationList.get(0), fullAllocationList.get(fullAllocationList.size() - 1))));
            for (int i = 1; i < fullAllocationList.size() - 1; i++) {
                Allocation thisAllocation = fullAllocationList.get(i);
                result.getAllocationList().add(result.getAllocationList().size() - 1, thisAllocation);
                result.getAllocationList().set(0,fullAllocationList.get(0));
                result.getAllocationList().set(result.getAllocationList().size()-1, fullAllocationList.get(fullAllocationList.size() - 1));
                DataStructureBuilder.constructChainProperty(result.getAllocationList());
                solvingStatus = 100 * result.getAllocationList().size() / fullAllocationList.size() + "%";
                if (thisAllocation.getJob().getJobType() == JobType.SCHEDULED && thisAllocation.getIndex() > result.getGlobalScheduleAfterIndex()) {
                    if (continuetosolve && !isSolved(result, currentSolver)) {
                        printCurrentSolution(result, currentSolver, false, solvingStatus);
                        currentSchedule = result;
                        currentSchedule = result = currentSolver.solve(result);
                        jsonServer.updateTimelineBlock(false, result);
                    }
                }
            }
        }
        
        //Solve Hard Full
        if (P1_mode.equals("global")) {
            currentSolver = solverList.get(0);
            DataStructureBuilder.constructChainProperty(result.getAllocationList());
            printCurrentSolution(result, solverList.get(0), true,solvingStatus);
            if (continuetosolve) currentSchedule = result = solverList.get(0).solve(result);
        }

        displayTray("Planning Done!", (getBestSolution() != null ? getBestSolution().getScore().toString() : ""));
        printCurrentSolution(result, solverList.get(0), true,solvingStatus);

        //Solve Soft
        if(P2_mode.equals("global")){
            currentSolver = solverList.get(1);
            if (continuetosolve) {
                currentSchedule = result = solverList.get(1).solve(result);
            }
        }

        jsonServer.updateTimelineBlock(false, result);
        return result;
    }

    public JsonServer getJsonServer() {
        return jsonServer;
    }

    public void setJsonServer(JsonServer jsonServer) {
        this.jsonServer = jsonServer;
    }

    public Object getResumeSolvingLock() {
        return resumeSolvingLock;
    }

    public void setResumeSolvingLock(Object resumeSolvingLock) {
        this.resumeSolvingLock = resumeSolvingLock;
    }

    public List<Solver<Schedule>> getSolverList() {
        return solverList;
    }

    public void setSolverList(List<Solver<Schedule>> solverList) {
        this.solverList = solverList;
    }

    public boolean isContinuetosolve() {
        return continuetosolve;
    }

    public void setContinuetosolve(boolean continuetosolve) {
        this.continuetosolve = continuetosolve;
    }

    public Object getNewTimelineBlockLock() {
        return newTimelineBlockLock;
    }

    public void setNewTimelineBlockLock(Object newTimelineBlockLock) {
        this.newTimelineBlockLock = newTimelineBlockLock;
    }

    private boolean isSolved(Schedule schedule, Solver solver) {
        ScoreDirector<Schedule> scoreDirector = solver.getScoreDirectorFactory().buildScoreDirector();
        scoreDirector.setWorkingSolution(schedule);
        for (ConstraintMatchTotal constraintMatch : scoreDirector.getConstraintMatchTotals()) {
            if (Arrays.stream(((BendableScore) constraintMatch.getScore()).getHardScores()).anyMatch(x -> x != 0))
                return false;
        }
        return true;
    }

}
