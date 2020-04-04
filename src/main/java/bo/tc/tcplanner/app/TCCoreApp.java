package bo.tc.tcplanner.app;

import bo.tc.tcplanner.Gui.TCGuiController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

import static bo.tc.tcplanner.PropertyConstants.*;
import static bo.tc.tcplanner.app.Toolbox.printCurrentSolution;

public class TCCoreApp extends TCApp {
    @Override
    public void start(Stage stage) throws Exception {
        //Initialization
        setConstants();

        //Declaration
        Object resumeSolvingLock = new Object();
        Object newSolutionLock = new Object();
        JsonServer jsonServer = new JsonServer();
        SolverThread solverThread = new SolverThread();
//        FirebaseServer firebaseServer = new FirebaseServer();

        //RMI Server
        try {
            rmiInterface = new RMIServer(solverThread);
            LocateRegistry.createRegistry(rmiport);
            Naming.rebind("//" + rmilistenadd + ":" + rmiport + "/MyServer", rmiInterface);
            System.err.println("Server ready");
        } catch (Exception e) {
            e.printStackTrace();

        }

        //JsonServer
        jsonServer.setSolverThread(solverThread);
//        jsonServer.setFirebaseServer(firebaseServer);
        jsonServer.setResumeSolvingLock(resumeSolvingLock);
        jsonServer.setNewTimelineBlockLock(newSolutionLock);

        //SolverThread
        solverThread.setJsonServer(jsonServer);
        solverThread.setResumeSolvingLock(new StringBuffer("ScheduleFile"));
        solverThread.setNewSolutionLock(newSolutionLock);

        //Start Threads
        jsonServer.createServer().start();
        solverThread.start();
//        firebaseServer.createServer().start();

        new Thread(() -> {
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
                    printCurrentSolution(solverThread.currentSchedule, true);
                } else {

                }
            }

        }).start();

        // Start Gui
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TCGui.fxml"));
        Parent root = loader.load();
        TCGuiController tcGuiController = loader.getController();
        tcGuiController.setApplication(this);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("TCCoreApp");
        stage.show();
    }


}
