package bo.tc.tcplanner.domain.solver.filters;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.move.generic.SwapMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class SameJobSwapMoveFilter implements SelectionFilter<Schedule, SwapMove> {
    @Override
    public boolean accept(ScoreDirector<Schedule> scoreDirector, SwapMove swapMove) {
        Allocation lallocation = (Allocation) swapMove.getLeftEntity();
        Allocation rallocation = (Allocation) swapMove.getRightEntity();
        return lallocation.getJob() == rallocation.getJob();
    }
}
