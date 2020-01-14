package bo.tc.tcplanner.app;

import bo.tc.tcplanner.datastructure.TimelineBlock;
import bo.tc.tcplanner.datastructure.converters.DataStructureBuilder;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import com.google.common.collect.Lists;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static bo.tc.tcplanner.app.TCSchedulingApp.timeHierarchyMap;
import static bo.tc.tcplanner.app.TCSchedulingApp.valueEntryMap;
import static bo.tc.tcplanner.app.Toolbox.*;
import static com.google.common.base.Preconditions.checkArgument;

public class SolverThread extends Thread {
    public static final Logger logger
            = LoggerFactory.getLogger(SolverThread.class);
    JsonServer jsonServer;
    boolean continuetosolve = true;
    String P1_mode = "global";
    String P2_mode = "global";
    String solvingStatus;
    Solver<Schedule> currentSolver;
    Schedule currentSchedule;
    private List<Solver<Schedule>> solverList;
    private Object resumeSolvingLock;
    private Object newTimelineBlockLock;

    public Solver<Schedule> getCurrentSolver() {
        return currentSolver;
    }

    public void setCurrentSolver(Solver<Schedule> currentSolver) {
        this.currentSolver = currentSolver;
    }

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
                initializeSolvers();
                runSolve();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void initializeSolvers() {
        SolverFactory<Schedule> solverFactory;
        solverList = new ArrayList<>();

        // Solve Phase 1 : to met requirements
        solverFactory = SolverFactory.createFromXmlResource("solverPhase1.xml");
        Solver<Schedule> solver1 = solverFactory.buildSolver();
        setSolverListener(solver1);
        solverList.add(solver1);

        // Solve Phase 2 : to optimize
        solverFactory = SolverFactory.createFromXmlResource("solverPhase2.xml");
        Solver<Schedule> solver2 = solverFactory.buildSolver();
        setSolverListener(solver2);
        solverList.add(solver2);

        currentSolver = solver1;
    }

    public void setSolverListener(Solver<Schedule> solver) {
        solver.addEventListener(bestSolutionChangedEvent -> {
            printCurrentSolution(bestSolutionChangedEvent.getNewBestSolution(), false, solvingStatus);
            currentSchedule = bestSolutionChangedEvent.getNewBestSolution();
            jsonServer.updateTimelineBlock(false, bestSolutionChangedEvent.getNewBestSolution());
        });
    }

    public Schedule getBestSolution() {
        for (Solver<Schedule> solver : Lists.reverse(solverList)) {
            if (solver.getBestSolution() != null) return solver.getBestSolution();
        }
        return null;
    }

    public void terminateSolver() {
        setContinuetosolve(false);
        if (solverList != null) for (Solver<Schedule> solver : solverList) solver.terminateEarly();
        while (solverList != null && !solverList.stream().noneMatch(Solver::isSolving)) {
        }
    }

    public void restartSolversWithNewTimelineBlock(TimelineBlock timelineBlock) {
        checkArgument(timelineBlock.checkValid());
        terminateSolver();
        try {
            Thread.sleep(1000);
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
        checkArgument(valueEntryMap.checkValid());
        checkArgument(timeHierarchyMap.checkValid());
        checkArgument(jsonServer.getLatestTimelineBlock().checkValid());

        continuetosolve = true;
        DataStructureBuilder DSB = new DataStructureBuilder(valueEntryMap, jsonServer.getLatestTimelineBlock(), timeHierarchyMap)
                .constructChainProperty();
        Schedule result = DSB.getSchedule();


//        Solve Hard Incremental By AllocationList
        if (P1_mode.equals("incremental")) {
            currentSolver = solverList.get(0);
            //Solve by restricted Score


            result.getAllocationList().forEach(x -> x.setScored(false));
            result.getAllocationList().forEach(x -> x.setPinned(true));
            result.getAllocationList().get(0).setScored(true);
            result.getAllocationList().get(result.getAllocationList().size() - 1).setScored(true);
            int solvingFrame = 800;
            for (int i = 1; i < result.getAllocationList().size() - 1; i++) {
                solvingStatus = 100 * (i + 1) / result.getAllocationList().size() + "%";
                Allocation thisAllocation = result.getAllocationList().get(i);
                thisAllocation.setScored(true);
                thisAllocation.setPinned(false);
                if (i > solvingFrame) {
                    result.getAllocationList().get(i - solvingFrame).setScored(false);
                    result.getAllocationList().get(i - solvingFrame).setPinned(true);
                }
                if (thisAllocation.isFocused() && !thisAllocation.isHistory() && i > solvingFrame) {
                    if (continuetosolve) {
                        printCurrentSolution(result, false, solvingStatus);
                        currentSchedule = result = currentSolver.solve(result);
                        jsonServer.updateTimelineBlock(false, result);
                    }
                }
            }
        }
//
//        //Solve Hard Full
//        if (P1_mode.equals("global")) {
//            currentSolver = solverList.get(0);
//            printCurrentSolution(result, false, solvingStatus);
//            currentSchedule = result;
//            if (continuetosolve) {
//                currentSchedule = result = solverList.get(0).solve(result);
//                jsonServer.updateTimelineBlock(false, result);
//            }
//        }
//
//        displayTray("Planning Done!", (getBestSolution() != null ? getBestSolution().getScore().toString() : ""));

        //Solve Soft
        if (P2_mode.equals("global")) {
            currentSolver = solverList.get(1);
            if (continuetosolve) {
                currentSchedule = result = solverList.get(1).solve(result);
//                jsonServer.updateTimelineBlock(false, result);
            }
        }
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

    private boolean isSolved(Schedule schedule, Solver<Schedule> solver) {
        ScoreDirector<Schedule> scoreDirector = createScoreDirector(schedule);
        scoreDirector.setWorkingSolution(schedule);
        for (ConstraintMatchTotal constraintMatch : scoreDirector.getConstraintMatchTotals()) {
            if (Arrays.stream(((BendableScore) constraintMatch.getScore()).getHardScores()).anyMatch(x -> x != 0))
                return false;
        }
        return true;
    }

}
