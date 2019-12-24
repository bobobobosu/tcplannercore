package bo.tc.tcplanner.app;

import bo.tc.tcplanner.SwiftGui.StartStopGui;
import bo.tc.tcplanner.datastructure.LocationHierarchyMap;
import bo.tc.tcplanner.datastructure.ValueEntryMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.RangeSet;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Scanner;

import static bo.tc.tcplanner.app.Toolbox.*;

public class TCSchedulingApp {
    public static LocationHierarchyMap locationHierarchyMap = null;
    public static HashMap<String, Object> timeHierarchyMap = null;
    public static ValueEntryMap valueEntryMap = null;
    public static HashMap<String, RangeSet<ZonedDateTime>> timeEntryMap;

    public static DateTimeFormatter dtf_TimelineEntry = DateTimeFormatter.ISO_ZONED_DATE_TIME;


    public static void main(String[] args) throws IOException {
        //Declaration
        Object resumeSolvingLock = new Object();
        Object newTimelineBlockLock = new Object();
        JsonServer jsonServer = new JsonServer();
        SolverThread solverThread = new SolverThread();

        //GUI
        new StartStopGui(solverThread);

        //JsonServer
        jsonServer.setSolverThread(solverThread);
        jsonServer.setResumeSolvingLock(resumeSolvingLock);
        jsonServer.setNewTimelineBlockLock(newTimelineBlockLock);

        //SolverThread
        solverThread.setJsonServer(jsonServer);
        solverThread.setResumeSolvingLock(resumeSolvingLock);
        solverThread.setNewTimelineBlockLock(newTimelineBlockLock);

        //Start Threads
        jsonServer.createServer().start();
        solverThread.start();


        new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String input = scanner.nextLine();
                    if (input.equals("p")) {
                        solverThread.terminateSolver();
                    } else if (input.equals("r")) {
                        solverThread.restartSolvers();
                    } else if (input.equals("x")) {
                        System.exit(0);
                    } else if (input.equals("e")) {
                        solverThread.getCurrentSolver().explainBestScore();
                    } else if (input.startsWith("t")) {
                        printCurrentSolution(solverThread.currentSchedule,true, solverThread.solvingStatus);
                    } else if (input.equals("p1i")) {
                        solverThread.P1_mode = "incremental";
                    } else if (input.equals("p1g")) {
                        solverThread.P1_mode = "global";
                    } else if (input.equals("p2g")) {
                        solverThread.P2_mode = "global";
                    } else {

                    }
                }

            }
        }).

                start();

    }

}
