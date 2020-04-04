package bo.tc.tcplanner.app;

import bo.tc.tcplanner.domain.Schedule;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import static bo.tc.tcplanner.app.JsonServer.flushConsole;

public class RMIServer extends UnicastRemoteObject implements RMIInterface {
    SolverThread solverThread;

    public RMIServer(SolverThread solverThread) throws RemoteException {
        super();
        this.solverThread = solverThread;
    }

    @Override
    public void startSolver(Schedule schedule) throws RemoteException {
        solverThread.terminateSolver();
        solverThread.currentSchedule = schedule;
        solverThread.resumeSolvers();
    }

    @Override
    public void stopSolver() throws RemoteException {
        solverThread.terminateSolver();
    }

    @Override
    public void resetSolver() throws RemoteException {
        solverThread.restartSolvers();
    }

    @Override
    public Schedule getCurrentSchedule() throws RemoteException {
        return solverThread.currentSchedule;
    }

    @Override
    public String getConsoleBuffer() throws RemoteException {
        return flushConsole();
    }

    @Override
    public Double getSolvingProgress() throws RemoteException {
        synchronized (solverThread.newSolutionLock) {
            try {
                solverThread.newSolutionLock.wait();
            } catch (InterruptedException ignored) {
            }
        }
        return Schedule.percentSolved(solverThread.currentSchedule);
    }
}
