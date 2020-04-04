package bo.tc.tcplanner.app;

import bo.tc.tcplanner.Gui.TCGuiController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.rmi.Naming;

import static bo.tc.tcplanner.PropertyConstants.rmiip;
import static bo.tc.tcplanner.PropertyConstants.rmiport;

public class TCRemoteApp extends TCApp {

    @Override
    public void start(Stage stage) throws Exception {
        // Connect to RMI Server
        if (getParameters().getNamed().containsKey("address")) {
            rmiip = getParameters().getNamed().get("address");
        }
        if (getParameters().getNamed().containsKey("port")) {
            rmiport = Integer.parseInt(getParameters().getNamed().get("port"));
        }
        rmiInterface = (RMIInterface) Naming.lookup("//" + rmiip + ":" + rmiport + "/MyServer");


        // Start Gui
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TCGui.fxml"));
        Parent root = loader.load();
        TCGuiController tcGuiController = loader.getController();
        tcGuiController.setApplication(this);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("TCRemoteApp");
        stage.show();

    }
}
