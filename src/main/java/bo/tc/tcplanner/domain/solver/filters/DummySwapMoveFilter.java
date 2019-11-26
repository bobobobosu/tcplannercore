package bo.tc.tcplanner.domain.solver.filters;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.move.generic.SwapMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import static bo.tc.tcplanner.datastructure.converters.DataStructureBuilder.dummyJob;
import static bo.tc.tcplanner.domain.solver.filters.FilterTools.*;

public class DummySwapMoveFilter implements SelectionFilter<Schedule, SwapMove> {

    @Override
    public boolean accept(ScoreDirector<Schedule> scoreDirector, SwapMove swapMove) {
        Allocation lallocation = (Allocation) swapMove.getLeftEntity();
        Allocation rallocation = (Allocation) swapMove.getRightEntity();
        if (isNotChangeable(lallocation)) return false;
        if (isNotChangeable(rallocation)) return false;
//        if(isNotInIndex(lallocation)) return false;
//        if(isNotInIndex(rallocation)) return false;
        if (isLocked(lallocation)) return false;
        if (isLocked(rallocation)) return false;

        return !((lallocation.getJob() == dummyJob && rallocation.getJob() == dummyJob) ||
                (lallocation.getJob() != dummyJob && rallocation.getJob() != dummyJob));
    }
}
