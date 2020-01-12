package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.domain.Allocation;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static bo.tc.tcplanner.domain.solver.listeners.ListenerTools.updateAllocationResourceStateChange;

public class ResourceStateChangeVariableListener implements VariableListener<Allocation> {
    Set<String> dirty = new TreeSet<>();

    @Override
    public void beforeEntityAdded(ScoreDirector scoreDirector, Allocation allocation) {

    }

    @Override
    public void afterEntityAdded(ScoreDirector scoreDirector, Allocation allocation) {
    }

    @Override
    public void beforeVariableChanged(ScoreDirector scoreDirector, Allocation allocation) {
        dirty.addAll(allocation.getTimelineEntry().getResourceStateChange().getResourceChange().keySet());
    }

    @Override
    public void afterVariableChanged(ScoreDirector scoreDirector, Allocation allocation) {
        dirty.addAll(allocation.getTimelineEntry().getResourceStateChange().getResourceChange().keySet());
        updateAllocation(scoreDirector, allocation);
        dirty = new TreeSet<>();
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector scoreDirector, Allocation allocation) {

    }

    @Override
    public void afterEntityRemoved(ScoreDirector scoreDirector, Allocation allocation) {
        updateAllocation(scoreDirector, allocation);
    }

    protected void updateAllocation(ScoreDirector scoreDirector, Allocation originalAllocation) {
        List<Allocation> focusedAllocationList = new ArrayList<>(originalAllocation.getFocusedAllocationSet());

        scoreDirector.beforeVariableChanged(originalAllocation, "resourceElementMap");
        originalAllocation.setResourceElementMap(null);
        scoreDirector.beforeVariableChanged(originalAllocation, "resourceElementMap");

        var newResourceElementMap = updateAllocationResourceStateChange(focusedAllocationList, dirty);
        for (int i = 0; i < focusedAllocationList.size(); i++) {
            scoreDirector.beforeVariableChanged(focusedAllocationList.get(i), "resourceElementMap");
            focusedAllocationList.get(i).setResourceElementMap(newResourceElementMap.get(i));
            scoreDirector.beforeVariableChanged(focusedAllocationList.get(i), "resourceElementMap");
        }

    }

}
