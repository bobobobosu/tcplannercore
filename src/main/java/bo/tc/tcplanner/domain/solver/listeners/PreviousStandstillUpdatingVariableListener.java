package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.domain.Allocation;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.Iterator;

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
        if (!originalAllocation.isFocused()) {
            originalAllocation.setPreviousStandstill(null);
        }
        Allocation startAllocation = originalAllocation.getFocusedAllocationSet().lower(originalAllocation);
        startAllocation = startAllocation == null ? originalAllocation : startAllocation;

        Iterator<Allocation> focusedAllocationIterator = originalAllocation.getFocusedAllocationSet()
                .tailSet(startAllocation).iterator();


        Allocation prevAllocation = focusedAllocationIterator.next();
        while (focusedAllocationIterator.hasNext()) {
            Allocation thisAllocation = focusedAllocationIterator.next();
            scoreDirector.beforeVariableChanged(thisAllocation, "previousStandstill");
            boolean changed = updateAllocationPreviousStandstill(thisAllocation, prevAllocation);
            scoreDirector.afterVariableChanged(thisAllocation, "previousStandstill");
            prevAllocation = thisAllocation;

            if (!changed && thisAllocation.getIndex() > originalAllocation.getIndex()) break;
        }

    }
}
