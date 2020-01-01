package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.domain.Allocation;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.List;

import static bo.tc.tcplanner.domain.solver.listeners.ListenerTools.updateAllocationPreviousStandstill;
import static bo.tc.tcplanner.domain.solver.listeners.ListenerTools.updatePredecessorsDoneDate;


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
        if (!originalAllocation.isFocused()) {
            originalAllocation.setPreviousStandstill(null);
        }

        List<Allocation> focusedAllocation = originalAllocation.getFocusedAllocationsTillEnd();
        for (int prevIdx = 0, thisIdx = 1; thisIdx < focusedAllocation.size(); prevIdx++, thisIdx++) {
            scoreDirector.beforeVariableChanged(focusedAllocation.get(thisIdx), "previousStandstill");
            updateAllocationPreviousStandstill(focusedAllocation.get(thisIdx), focusedAllocation.get(prevIdx));
            scoreDirector.afterVariableChanged(focusedAllocation.get(thisIdx), "previousStandstill");
        }

    }

}
