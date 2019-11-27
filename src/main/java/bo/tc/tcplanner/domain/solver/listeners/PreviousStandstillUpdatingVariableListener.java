package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import static bo.tc.tcplanner.datastructure.converters.DataStructureBuilder.dummyJob;
import static bo.tc.tcplanner.domain.solver.listeners.ListenerTools.updateAllocationPreviousStandstill;
import static bo.tc.tcplanner.domain.solver.listeners.ListenerTools.updateAllocationResourceStateChange;


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
        NonDummyAllocationIterator nonDummyAllocationIterator;
        while (originalAllocation.getPreviousStandstill() == null){
            originalAllocation = NonDummyAllocationIterator.getPrev(originalAllocation);
        }
        nonDummyAllocationIterator = new NonDummyAllocationIterator(originalAllocation);
        Allocation prevAllocation = nonDummyAllocationIterator.next();
        while (nonDummyAllocationIterator.hasNext()) {
            Allocation thisAllocation = nonDummyAllocationIterator.next();
            scoreDirector.beforeVariableChanged(thisAllocation, "resourceElementMap");
            updateAllocationPreviousStandstill(thisAllocation, prevAllocation);
            scoreDirector.afterVariableChanged(thisAllocation, "resourceElementMap");
            prevAllocation = thisAllocation;
        }
    }

}
