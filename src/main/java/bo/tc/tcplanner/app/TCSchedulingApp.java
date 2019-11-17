package bo.tc.tcplanner.app;

import bo.tc.tcplanner.SwiftGui.StartStopGui;
import bo.tc.tcplanner.datastructure.LocationHierarchyMap;
import bo.tc.tcplanner.datastructure.ValueEntryMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Scanner;

import static bo.tc.tcplanner.app.Toolbox.*;

public class TCSchedulingApp {
    public static String fpath_Constants = "C:\\_DATA\\_Storage\\_Sync\\Devices\\root\\Constants.json";
    public static String path_Notebook;
    public static String fpath_TimelineBlock;
    public static String fpath_ValueEntryMap;
    public static String fpath_LocationHierarchyMap;
    public static String fpath_TimeHierarchyMap;

    public static LocationHierarchyMap locationHierarchyMap = null;
    public static HashMap<String, Object> timeHierarchyMap = null;
    public static ValueEntryMap valueEntryMap = null;

    public static DateTimeFormatter dtf_TimelineEntry = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    public static DateTimeFormatter dtf_Dhtmlxgantt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public static void main(String[] args) throws IOException {
        //Declaration
        Object resumeSolvingLock = new Object();
        Object newTimelineBlockLock = new Object();
        JsonServer jsonServer = new JsonServer();
        SolverThread solverThread = new SolverThread();
        setConstants();

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
                        solverThread.printCurrentSolution(solverThread.currentSchedule,solverThread.currentSolver,true);
                    } else {

                    }
                }

            }
        }).

                start();

    }

    static void setConstants() throws IOException {
        HashMap ConstantsJson = new ObjectMapper().readValue(
                IOUtils.toString(new FileInputStream(new File(fpath_Constants)), StandardCharsets.UTF_8), HashMap.class);
        path_Notebook = castString(castDict(castDict(ConstantsJson.get("Paths")).get("Folders")).get("Notebook"));
        fpath_TimelineBlock = genPathFromConstants("TimelineBlock.json", ConstantsJson);
        fpath_ValueEntryMap = genPathFromConstants("ValueEntryMap.json", ConstantsJson);
        fpath_LocationHierarchyMap = genPathFromConstants("LocationHierarchyMap.json", ConstantsJson);
        fpath_TimeHierarchyMap = genPathFromConstants("TimeHierarchyMap.json", ConstantsJson);
    }


}
