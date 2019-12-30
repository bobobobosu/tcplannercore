package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.datastructure.ResourceElement;
import bo.tc.tcplanner.domain.Allocation;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.*;
import java.util.stream.Collectors;

import static bo.tc.tcplanner.app.DroolsTools.locationRestrictionCheck;
import static bo.tc.tcplanner.datastructure.converters.DataStructureBuilder.dummyJob;
import static bo.tc.tcplanner.domain.solver.listeners.ListenerTools.deepCloneResourceMap;
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
        List<Allocation> focusedAllocationList = NonDummyAllocationIterator.getAllNextIncludeThis(originalAllocation.getSourceAllocation());

        focusedAllocationList.forEach(x -> scoreDirector.beforeVariableChanged(x, "resourceElementMap"));

        updateAllocationResourceStateChange(focusedAllocationList);

        focusedAllocationList.forEach(x -> scoreDirector.afterVariableChanged(x, "resourceElementMap"));
    }

}
