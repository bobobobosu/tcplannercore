package bo.tc.tcplanner.domain.solver.filters;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import static bo.tc.tcplanner.domain.solver.filters.FilterTools.isLocked;

public class UnlockedAllocationFilter implements SelectionFilter<Schedule, Allocation> {
    @Override
    public boolean accept(ScoreDirector<Schedule> scoreDirector, Allocation allocation) {
        return !isLocked(allocation);
    }
}
