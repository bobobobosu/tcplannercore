package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.domain.Allocation;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import static bo.tc.tcplanner.domain.solver.listeners.ListenerTools.updateAllocationResourceStateChange;

public class ResourceStateChangeVariableListener implements VariableListener<Allocation> {
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
        updateAllocation(scoreDirector, allocation);
    }

    protected void updateAllocation(ScoreDirector scoreDirector, Allocation originalAllocation) {
        NonDummyAllocationIterator nonDummyAllocationIterator = new NonDummyAllocationIterator(
                originalAllocation);
        Allocation prevAllocation = NonDummyAllocationIterator.getPrev(originalAllocation);
//        if(prevAllocation != null) System.out.println(prevAllocation + " " + originalAllocation);
        while (nonDummyAllocationIterator.hasNext()) {
            Allocation thisAllocation = nonDummyAllocationIterator.next();
            scoreDirector.beforeVariableChanged(thisAllocation, "resourceElementMap");
            updateAllocationResourceStateChange(thisAllocation, prevAllocation);
            scoreDirector.afterVariableChanged(thisAllocation, "resourceElementMap");
            prevAllocation = thisAllocation;
        }

    }

}
