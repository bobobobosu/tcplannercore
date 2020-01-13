package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.domain.Allocation;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.Iterator;

import static bo.tc.tcplanner.domain.solver.listeners.ListenerTools.updatePlanningDuration;
import static bo.tc.tcplanner.domain.solver.listeners.ListenerTools.updatePredecessorsDoneDate;

public class PlanningDurationVariableUpdatingListener  implements VariableListener<Allocation> {

    @Override
    public void beforeEntityAdded(ScoreDirector scoreDirector, Allocation allocation) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(ScoreDirector scoreDirector, Allocation allocation) {
        updateAllocation(scoreDirector, allocation);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector scoreDirector, Allocation allocation) {
        // Do nothing
    }

    @Override
    public void afterVariableChanged(ScoreDirector scoreDirector, Allocation allocation) {
        updateAllocation(scoreDirector, allocation);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector scoreDirector, Allocation allocation) {
        // Do nothing
    }

    @Override
    public void afterEntityRemoved(ScoreDirector scoreDirector, Allocation allocation) {
        // Do nothing
    }

    protected void updateAllocation(ScoreDirector scoreDirector, Allocation originalAllocation) {
        // Update Planning Duration
        scoreDirector.beforeVariableChanged(originalAllocation, "plannedDuration");
        if (!originalAllocation.isFocused()) {
            originalAllocation.setPlannedDuration(null);
        } else {
            updatePlanningDuration(originalAllocation);
        }
        scoreDirector.afterVariableChanged(originalAllocation, "plannedDuration");

    }
}
