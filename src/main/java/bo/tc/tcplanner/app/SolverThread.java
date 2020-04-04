package bo.tc.tcplanner.app;

import bo.tc.tcplanner.PropertyConstants.SolverPhase;
import bo.tc.tcplanner.datastructure.TimelineBlock;
import bo.tc.tcplanner.datastructure.converters.DataStructureBuilder;
import bo.tc.tcplanner.domain.Schedule;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.solver.DefaultSolver;
import org.optaplanner.persistence.xstream.impl.domain.solution.XStreamSolutionFileIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static bo.tc.tcplanner.PropertyConstants.fpath_ScheduleSolution;
import static bo.tc.tcplanner.PropertyConstants.initializeFiles;
import static bo.tc.tcplanner.app.JsonServer.updateConsole;
import static bo.tc.tcplanner.PropertyConstants.*;
import static bo.tc.tcplanner.app.Toolbox.displayTray;
import static bo.tc.tcplanner.app.Toolbox.printCurrentSolution;
import static com.google.common.base.Preconditions.checkArgument;

public class SolverThread extends Thread {
    public static final Logger logger
            = LoggerFactory.getLogger(SolverThread.class);

    // links
    JsonServer jsonServer;
    boolean continuetosolve = true;
    // current
    public Solver<Schedule> currentSolver;
    static Object[] lastNewBestSolution = {0L, 0L, null, null};
    public Schedule currentSchedule;
    private List<Solver<Schedule>> solverList;
    // locks
    private StringBuffer resumeSolvingLock;
    public Object newSolutionLock;

    public static ScoreDirector<Schedule> getScoringScoreDirector() {
        ScoreDirector<Schedule> scoringScoreDirector;
        SolverFactory solverFactory = SolverFactory.createFromXmlResource("solverPhase1.xml");
        scoringScoreDirector = solverFactory.getScoreDirectorFactory().buildScoreDirector();
        return scoringScoreDirector;
    }

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
            } catch (IllegalArgumentException | IOException ex) {
                String msg = Throwables.getStackTraceAsString(ex);
                System.err.println(updateConsole(msg));
            }
        }
    }

    public void initializeSolvers() {
        SolverFactory<Schedule> solverFactory;
        SolverConfig solverConfig0 = SolverConfig.createFromXmlResource("solverPhase0.xml");
        SolverConfig solverConfig1 = SolverConfig.createFromXmlResource("solverPhase1.xml");
        SolverConfig solverConfig2 = SolverConfig.createFromXmlResource("solverPhase2.xml");
        solverList = new ArrayList<>();

        // Solve Phase 0 : construction heuristic
        /*
        From template, remove local search phase & termination config
         */
        solverConfig0.withTerminationConfig(
                new TerminationConfig());
        solverFactory = SolverFactory.create(solverConfig0);
        DefaultSolver<Schedule> solver0 = (DefaultSolver<Schedule>) solverFactory.buildSolver();
        solver0.getSolverScope().getScoreDirector().overwriteConstraintMatchEnabledPreference(true);
        setSolverListener(solver0);
        solverList.add(solver0);

        // Solve Phase 1 : fast
        /*
        From template, remove construction heuristic phase & termination config and set accepted count limit 350
         */
        solverConfig1.withTerminationConfig(
                new TerminationConfig()
                        .withUnimprovedSecondsSpentLimit(3L)
                        .withBestScoreFeasible(true));
        solverFactory = SolverFactory.create(solverConfig1);
        Solver<Schedule> solver1 = solverFactory.buildSolver();
        setSolverListener(solver1);
        solverList.add(solver1);

        // Solve Phase 2 : accurate
        /*
        From template, remove construction heuristic phase & termination config & pick early type
         */
        solverConfig2.withTerminationConfig(
                new TerminationConfig()
                        .withUnimprovedSecondsSpentLimit(90L)
                        .withBestScoreFeasible(true));
        solverFactory = SolverFactory.create(solverConfig2);
        Solver<Schedule> solver2 = solverFactory.buildSolver();
        setSolverListener(solver2);
        solverList.add(solver2);

        // Solve Phase 3 : optimize
        /*
        Same as Phase 2
         */
        solverConfig2.withTerminationConfig(
                new TerminationConfig()
                        .withUnimprovedMinutesSpentLimit(5L));
        solverFactory = SolverFactory.create(solverConfig2);
        Solver<Schedule> solver3 = solverFactory.buildSolver();
        setSolverListener(solver3);
        solverList.add(solver3);

        currentSolver = solverList.get(1);
    }

    public void setSolverListener(Solver<Schedule> solver) {
        solver.addEventListener(bestSolutionChangedEvent -> {
            long currT = System.currentTimeMillis();
            lastNewBestSolution[1] = currT - (long) lastNewBestSolution[0];
            lastNewBestSolution[0] = currT;
            lastNewBestSolution[3] = lastNewBestSolution[2] != null ?
                    solver.getBestScore().subtract((Score) lastNewBestSolution[2]) : null;
            lastNewBestSolution[2] = solver.getBestScore();
            printCurrentSolution(bestSolutionChangedEvent.getNewBestSolution(), false);
            currentSchedule = bestSolutionChangedEvent.getNewBestSolution();
            synchronized (newSolutionLock) {
                newSolutionLock.notify();
            }
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
        while (solverList != null && solverList.stream().anyMatch(Solver::isSolving)) {
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
        resumeSolvingLock.setLength(0);
        resumeSolvingLock.append("TimelineBlock");
        synchronized (resumeSolvingLock) {
            setContinuetosolve(true);
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
        resumeSolvingLock.setLength(0);
        resumeSolvingLock.append("Restart");
        synchronized (resumeSolvingLock) {
            setContinuetosolve(true);
            resumeSolvingLock.notify();
        }
    }

    public void resumeSolvers() {
        resumeSolvingLock.setLength(0);
        resumeSolvingLock.append("Resume");
        synchronized (resumeSolvingLock) {
            setContinuetosolve(true);
            resumeSolvingLock.notify();
        }
    }

    public void runSolve() throws IOException {
        if (resumeSolvingLock.toString().equals("TimelineBlock")) {
            checkArgument(valueEntryMap.checkValid());
            checkArgument(timeHierarchyMap.checkValid());
            checkArgument(jsonServer.getProblemTimelineBlock().checkValid());

            continuetosolve = true;
            DataStructureBuilder DSB = new DataStructureBuilder(valueEntryMap, jsonServer.getProblemTimelineBlock(), timeHierarchyMap, locationHierarchyMap)
                    .constructChainProperty();
            currentSchedule = DSB.getSchedule();
        } else if (resumeSolvingLock.toString().equals("Restart")) {
            if (valueEntryMap == null || locationHierarchyMap == null || timeHierarchyMap == null) initializeFiles();
            currentSchedule = new XStreamSolutionFileIO<Schedule>().read(new File(fpath_ScheduleSolution));
        } else if (resumeSolvingLock.toString().equals("Resume")) {

        } else {
            return;
        }

        //Solve Hard
        while (continuetosolve && !isSolved(currentSchedule)) {
            printCurrentSolution(currentSchedule, false);
            currentSolver = solverList.get(0);
            if (continuetosolve) {
                currentSchedule.solverPhase = SolverPhase.CH;
                currentSolver.solve(currentSchedule);
                jsonServer.updateTimelineBlock(false, currentSchedule);
            }
            currentSolver = solverList.get(1);
            if (continuetosolve) {
                currentSchedule.solverPhase = SolverPhase.FAST;
                currentSolver.solve(currentSchedule);
                jsonServer.updateTimelineBlock(false, currentSchedule);
            }
            currentSolver = solverList.get(2);
            if (continuetosolve) {
                currentSchedule.solverPhase = SolverPhase.ACCURATE;
                currentSolver.solve(currentSchedule);
                jsonServer.updateTimelineBlock(false, currentSchedule);
            }
        }
        displayTray("Planning Done!", (getBestSolution() != null ? getBestSolution().getScore().toString() : ""));
        ScoreDirector<Schedule> gg = getScoringScoreDirector();
        gg.setWorkingSolution(currentSchedule);
        gg.calculateScore();
        var weade = gg.getConstraintMatchTotalMap();
        var ggh = gg.getIndictmentMap();

        //Solve Soft
        currentSolver = solverList.get(3);
        if (continuetosolve) {
            currentSchedule.solverPhase = SolverPhase.REDUCE;
            currentSolver.solve(currentSchedule);
            jsonServer.updateTimelineBlock(false, currentSchedule);
        }

        new XStreamSolutionFileIO<>(Schedule.class).write(currentSchedule, new File(fpath_ScheduleSolution));
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

    public void setResumeSolvingLock(StringBuffer resumeSolvingLock) {
        this.resumeSolvingLock = resumeSolvingLock;
    }

    public Object getNewSolutionLock() {
        return newSolutionLock;
    }

    public void setNewSolutionLock(Object newSolutionLock) {
        this.newSolutionLock = newSolutionLock;
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

    private boolean isSolved(Schedule schedule) {
        ScoreDirector<Schedule> scoreDirector = getScoringScoreDirector();
        scoreDirector.setWorkingSolution(schedule);
        scoreDirector.calculateScore();
        return !(((HardMediumSoftLongScore) scoreDirector.calculateScore()).getHardScore() < 0);
    }
}

