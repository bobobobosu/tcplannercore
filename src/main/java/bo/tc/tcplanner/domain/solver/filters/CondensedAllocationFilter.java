package bo.tc.tcplanner.domain.solver.filters;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.Iterator;

public class CondensedAllocationFilter implements SelectionFilter<Schedule, Allocation> {
    @Override
    public boolean accept(ScoreDirector<Schedule> scoreDirector, Allocation allocation) {
        if (allocation.isFocused()) return true;
        Allocation prevAllocation = allocation.getPrevAllocation();
        Allocation nextAllocation = allocation.getNextAllocation();
        if (prevAllocation == null || nextAllocation == null) return true;
        int mid = (prevAllocation.getIndex() + nextAllocation.getIndex()) / 2;
        return allocation.getIndex() == mid;
    }


}
