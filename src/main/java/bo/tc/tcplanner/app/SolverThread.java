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
import static bo.tc.tcplanner.app.Toolbox.OffsetMinutes2ZonedDatetime;
import static bo.tc.tcplanner.app.Toolbox.displayTray;
import static bo.tc.tcplanner.domain.DataStructureBuilder.dummyJob;

public class SolverThread extends Thread {
    JsonServer jsonServer;
    boolean continuetosolve = true;
    String solvingStatus;

    public Solver getCurrentSolver() {
        return currentSolver;
    }

    public void setCurrentSolver(Solver currentSolver) {
        this.currentSolver = currentSolver;
    }

    Solver currentSolver;
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
            displayTray("Planning Done!", (getBestSolution() != null ? getBestSolution().getScore().toString() : ""));
        }
    }


    public Schedule initializeData(TimelineBlock latestTimelineBlock) throws IOException {
        //Proprocess TEBlock
//        Collections.sort(latestTimelineBlock.getTimelineEntryList(), new Comparator<TimelineEntry>() {
//            @Override
//            public int compare(TimelineEntry o1, TimelineEntry o2) {
//                return o1.getStartTime().compareTo(o2.getStartTime());
//            }
//        });
        // Build DataStructure
        DataStructureBuilder DSB = new DataStructureBuilder();
        DSB.setGlobalProperties(latestTimelineBlock);
        DSB.addResourcesFromValueEntryMap(valueEntryMap, DSB.getDefaultProject());
        DSB.addJobsFromValueEntryDict(valueEntryMap, DSB.getDefaultProject());
        DSB.addJobsFromTimelineBlock(latestTimelineBlock, DSB.getDefaultProject());
        DSB.initializeAllocationList(20);
        DataStructureBuilder.constructChainProperty(DSB.getListOfAllocations());
        return DSB.getDefaultSchedule();
    }

    public void initializeFiles() {
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
        solver.addEventListener(new SolverEventListener<Schedule>() {
            @Override
            public void bestSolutionChanged(BestSolutionChangedEvent<Schedule> bestSolutionChangedEvent) {
                printCurrentSolution(bestSolutionChangedEvent.getNewBestSolution(), solver);
                jsonServer.updateTimelineBlock(false, bestSolutionChangedEvent.getNewBestSolution());
//                jsonServer.saveFiles();
            }
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

    public Schedule runSolve() throws IOException {
        Schedule result = null;
        continuetosolve = true;
        try {
            result = initializeData(jsonServer.getLatestTimelineBlock());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Solve Hard Incremental By AllocationList
        if (false) {
            currentSolver = solverList.get(0);
            List<Allocation> incrementalAllocationList = new ArrayList<>();
            List<Allocation> fullAllocationList = new ArrayList<>(result.getAllocationList());
            Allocation source = fullAllocationList.get(0);
            Allocation sink = fullAllocationList.get(fullAllocationList.size() - 1);
            for (Allocation allocation : fullAllocationList) {
                if (allocation == source || allocation == sink) continue;
                incrementalAllocationList.add(allocation);
                solvingStatus = 100 * incrementalAllocationList.size() / (fullAllocationList.size() - 2) + "%";
                if (allocation.getJob().getJobType() == JobType.MANDATORY && allocation.getIndex() > result.getGlobalScheduleAfterIndex()) {
                    incrementalAllocationList.add(0, source);
                    incrementalAllocationList.add(sink);
                    DataStructureBuilder.constructChainProperty(incrementalAllocationList);
                    result.setAllocationList(incrementalAllocationList);
//                    printCurrentSolution(result, solverList.get(0));
                    if (continuetosolve && !isSolved(result, solverList.get(0))) {
                        result = solverList.get(0).solve(result);
                        jsonServer.updateTimelineBlock(false, result);
                    }
                    incrementalAllocationList = new ArrayList<>(result.getAllocationList());
                    incrementalAllocationList.remove(0);
                    incrementalAllocationList.remove(incrementalAllocationList.size() - 1);
                }

            }
        }

        //Solve Hard Full
        if (true) {
            currentSolver = solverList.get(0);
            printCurrentSolution(result, solverList.get(0));
            if (continuetosolve) result.setAllocationList(solverList.get(0).solve(result).getAllocationList());
        }

        //Solve Soft
        currentSolver = solverList.get(1);
        if (continuetosolve) {
            result.setAllocationList(solverList.get(1).solve(result).getAllocationList());
            List<ExecutionMode> newExecutionModeList = new ArrayList<>();
            newExecutionModeList.add(DataStructureBuilder.dummyExecutionMode);
            result.setExecutionModeList(newExecutionModeList);
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

    public void printCurrentSolution(Schedule schedule, Solver solver) {
//        System.out.println(solver.explainBestScore());
//        return;
        try {
            System.err.print("\033[H\033[2J");
            System.err.flush();
            String[] breakByRulesHeader = {"Break Up By Rule"};
            String[] timelineHeader = {"Row", "%", "Date", "Duration", "Location", "Restriction", "Score", "Task"};
            List<String[]> breakByRules = new ArrayList();
            Map<Allocation, Indictment> breakByTasks = new HashMap<>();
            List<String[]> timeline = new ArrayList();


            ScoreDirector<Schedule> scoreDirector = solver.getScoreDirectorFactory().buildScoreDirector();
            scoreDirector.setWorkingSolution(schedule);
            for (ConstraintMatchTotal constraintMatch : scoreDirector.getConstraintMatchTotals()) {
                if (Arrays.stream(((BendableScore) constraintMatch.getScore()).getHardScores()).anyMatch(x -> x != 0))
                    breakByRules.add(new String[]{constraintMatch.toString()});
            }
            for (Map.Entry<Object, Indictment> indictmentEntry : scoreDirector.getIndictmentMap().entrySet()) {
                if (indictmentEntry.getValue().getJustification() instanceof Allocation &&
                        Arrays.stream(((BendableScore) indictmentEntry.getValue().getScore()).getHardScores()).anyMatch(x -> x != 0)) {
                    Allocation matchAllocation = (Allocation) indictmentEntry.getValue().getJustification();
                    breakByTasks.put(matchAllocation, indictmentEntry.getValue());
                }
            }
            for (Allocation allocation : schedule.getAllocationList()) {
                if (allocation.getJob() != dummyJob) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm");
                    String datetime = formatter.format(OffsetMinutes2ZonedDatetime(allocation.getProject().getSchedule().getGlobalStartTime(),
                            allocation.getStartDate()).withZoneSameInstant(ZoneId.systemDefault())) + "\n" +
                            formatter.format(OffsetMinutes2ZonedDatetime(allocation.getProject().getSchedule().getGlobalStartTime(),
                                    allocation.getEndDate()).withZoneSameInstant(ZoneId.systemDefault()));
                    timeline.add(new String[]{
                            (allocation.getJob().getRownum() == null || allocation.getJob().getRownum() < 0) ? "****" : String.valueOf(allocation.getJob().getRownum()),
                            String.valueOf(allocation.getProgressdelta()),
                            datetime,
                            LocalTime.MIN.plus(Duration.ofMinutes(allocation.getEndDate() - allocation.getStartDate())).toString(),
                            "P:" + allocation.getPreviousStandstill() +
                                    "\nC:" + allocation.getExecutionMode().getCurrentLocation()+
                                    "\nM:" + allocation.getExecutionMode().getMovetoLocation()
                                    ,
                            allocation.getJob().getChangeable() + "C/" +
                                    allocation.getJob().getMovable() + "M/" +
                                    allocation.getJob().getSplittable() + "S/" +
                                    ((allocation.getAllocationType() == AllocationType.Locked) ? 1 : 0) + "L",
                            (breakByTasks.containsKey(allocation) ? "\n" + Arrays.toString(((BendableScore) breakByTasks.get(allocation).getScore()).getHardScores()) : ""),
                            allocation.getJob().getName() + " " + allocation.getId()
                    });
                }
            }
            timeline.add(timelineHeader);
            solver.explainBestScore();
            System.err.println(FlipTable.of(timelineHeader, timeline.toArray(new String[timeline.size()][])));
            System.err.println(FlipTable.of(breakByRulesHeader, breakByRules.toArray(new String[breakByRules.size()][])));
            System.err.println("Status: " + solvingStatus + " " + new SimpleDateFormat("dd-MM HH:mm").format(new Date()));
        } catch (Exception ex) {
//                    ex.printStackTrace();
        }
    }
}
