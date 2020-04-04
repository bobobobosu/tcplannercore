package bo.tc.tcplanner.app;

import bo.tc.tcplanner.domain.Schedule;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIInterface extends Remote {
    void startSolver(Schedule schedule) throws RemoteException;

    void stopSolver() throws RemoteException;

    void resetSolver() throws RemoteException;

    Schedule getCurrentSchedule() throws RemoteException;

    String getConsoleBuffer()  throws RemoteException;

    Double getSolvingProgress() throws RemoteException;

}