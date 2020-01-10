package bo.tc.tcplanner.domain.solver.meters;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionProbabilityWeightFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class AllocationProbabilityWeightFactory implements SelectionProbabilityWeightFactory<Schedule, Allocation> {
    @Override
    public double createProbabilityWeight(ScoreDirector<Schedule> scoreDirector, Allocation allocation) {
//        double closestDistance = Double.MAX_VALUE;
//        for (Map.Entry<Object, Indictment> indictmentEntry : scoreDirector.getIndictmentMap().entrySet()) {
//            if (indictmentEntry.getValue().getJustification() instanceof Allocation &&
//                    Arrays.stream(((BendableScore) indictmentEntry.getValue().getScore()).getHardScores()).anyMatch(x -> x != 0)) {
//                Allocation matchAllocation = (Allocation) indictmentEntry.getValue().getJustification();
//                closestDistance = Math.min(closestDistance, Math.abs(matchAllocation.getIndex() - allocation.getIndex()));
//            }
//        }
//        return closestDistance == 0 ? 1 : 1 / closestDistance;
        return (allocation.isPinned() ? 0 : allocation.getIndex());
//        return 0.1;
    }
}
