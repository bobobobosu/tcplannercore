package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.ArrayDeque;
import java.util.Queue;

import static bo.tc.tcplanner.domain.solver.listeners.ListenerTools.updateAllocationPreviousStandstill;


public class PreviousStandstillUpdatingVariableListener implements VariableListener<Allocation> {
    @Override
    public void beforeEntityAdded(ScoreDirector scoreDirector, Allocation allocation) {

    }

    @Override
    public void afterEntityAdded(ScoreDirector scoreDirector, Allocation allocation) {
        updateAllocation(scoreDirector, allocation);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector scoreDirector, Allocation allocation) {

    }

    @Override
    public void afterVariableChanged(ScoreDirector scoreDirector, Allocation allocation) {
        updateAllocation(scoreDirector, allocation);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector scoreDirector, Allocation allocation) {

    }

    @Override
    public void afterEntityRemoved(ScoreDirector scoreDirector, Allocation allocation) {

    }

    protected void updateAllocation(ScoreDirector scoreDirector, Allocation originalAllocation) {
        Schedule schedule = (Schedule) scoreDirector.getWorkingSolution();
        Queue<Allocation> uncheckedSuccessorQueue = new ArrayDeque<>();
        uncheckedSuccessorQueue.addAll(originalAllocation.getSuccessorAllocationList());
        while (!uncheckedSuccessorQueue.isEmpty()) {
            Allocation allocation = uncheckedSuccessorQueue.remove();
            boolean updated = updatePreviousStandstill(scoreDirector, allocation);
            if (updated) {
                uncheckedSuccessorQueue.addAll(allocation.getSuccessorAllocationList());
            }
        }
        int i = 0;
    }

    protected boolean updatePreviousStandstill(ScoreDirector scoreDirector, Allocation allocation) {
        scoreDirector.beforeVariableChanged(allocation, "previousStandstill");
        updateAllocationPreviousStandstill(allocation);
        scoreDirector.afterVariableChanged(allocation, "previousStandstill");
        return true;
    }
}
