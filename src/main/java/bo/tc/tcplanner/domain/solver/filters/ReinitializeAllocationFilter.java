package bo.tc.tcplanner.domain.solver.filters;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class ReinitializeAllocationFilter implements SelectionFilter<Schedule, Allocation> {
    CondensedAllocationFilter condensedAllocationFilter = new CondensedAllocationFilter();

    @Override
    public boolean accept(ScoreDirector<Schedule> scoreDirector, Allocation selection) {
        if (condensedAllocationFilter.accept(scoreDirector, selection)) {
            Allocation allocationToCheck = selection.getNextAllocation();
            return scoreDirector.getIndictmentMap().containsKey(allocationToCheck) &&
                    ((HardMediumSoftLongScore) scoreDirector.getIndictmentMap().get(allocationToCheck).getScore())
                            .getHardScore() < 0;
        }
        return false;
    }
}
