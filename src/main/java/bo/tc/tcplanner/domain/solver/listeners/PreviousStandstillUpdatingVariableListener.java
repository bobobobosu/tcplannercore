package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.domain.Allocation;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import static bo.tc.tcplanner.datastructure.converters.DataStructureBuilder.dummyJob;
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
        if (originalAllocation.getJob() == dummyJob) {
            originalAllocation.setPreviousStandstill(null);
        }

        // Start from prev to update this
        originalAllocation =
                NonDummyAllocationIterator.getPrev(originalAllocation) != null ?
                        NonDummyAllocationIterator.getPrev(originalAllocation) :
                        originalAllocation;

        while (originalAllocation.getPreviousStandstill() == null) {
            originalAllocation = NonDummyAllocationIterator.getPrev(originalAllocation);
        }
        Allocation prevAllocation = originalAllocation;
        Allocation thisAllocation;
        while ((thisAllocation = NonDummyAllocationIterator.getNext(prevAllocation)) != null) {
            scoreDirector.beforeVariableChanged(thisAllocation, "previousStandstill");
            updateAllocationPreviousStandstill(thisAllocation, prevAllocation);
            scoreDirector.afterVariableChanged(thisAllocation, "previousStandstill");
            prevAllocation = thisAllocation;
        }
    }

}
