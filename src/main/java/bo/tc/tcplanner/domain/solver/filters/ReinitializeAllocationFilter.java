package bo.tc.tcplanner.domain.solver.filters;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.Arrays;
import java.util.Map;

public class ReinitializeAllocationFilter implements SelectionFilter<Schedule, Allocation> {
    CondensedAllocationFilter condensedAllocationFilter = new CondensedAllocationFilter();

    @Override
    public boolean accept(ScoreDirector<Schedule> scoreDirector, Allocation selection) {
        if (condensedAllocationFilter.accept(scoreDirector, selection)) {
            Allocation allocationToCheck = selection.getNextAllocation();
            return scoreDirector.getIndictmentMap().containsKey(allocationToCheck) &&
                    Arrays.stream(((BendableScore) scoreDirector.getIndictmentMap().get(allocationToCheck).getScore())
                            .getHardScores()).anyMatch(x -> x < 0);
        }
        return false;
    }
}
