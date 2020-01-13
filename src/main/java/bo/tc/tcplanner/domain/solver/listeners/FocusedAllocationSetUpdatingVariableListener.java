package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.domain.Allocation;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class FocusedAllocationSetUpdatingVariableListener implements VariableListener<Allocation> {

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

        scoreDirector.beforeVariableChanged(originalAllocation, "focusedAllocationSet");
        if (originalAllocation.isFocused()) {
            originalAllocation.getFocusedAllocationSet().add(originalAllocation);
        } else {
            originalAllocation.getFocusedAllocationSet().remove(originalAllocation);
        }
        scoreDirector.afterVariableChanged(originalAllocation, "focusedAllocationSet");

    }
}
