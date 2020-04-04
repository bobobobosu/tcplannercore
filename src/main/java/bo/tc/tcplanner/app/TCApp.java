package bo.tc.tcplanner.app;

import javafx.application.Application;

public abstract class TCApp extends Application {
    RMIInterface rmiInterface;

    public RMIInterface getRmiInterface() {
        return rmiInterface;
    }

    public void setRmiInterface(RMIInterface rmiInterface) {
        this.rmiInterface = rmiInterface;
    }
}
