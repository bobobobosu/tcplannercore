package bo.tc.tcplanner.domain.solver.score;

import bo.tc.tcplanner.domain.Allocation;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

import static bo.tc.tcplanner.app.DroolsTools.locationRestrictionCheck;

public class ScheduleConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                checkDependencyId(constraintFactory),
                checkPreviousStandstill(constraintFactory)};
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************
    private Constraint checkDependencyId(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isScored() && allocation.isFocused())
                .join(Allocation.class)
                .filter(((allocation, allocation2) ->
                        allocation2.isFocused() && allocation2.getIndex() > allocation2.getIndex() &&
                                allocation.getTimelineEntry().getTimelineProperty().getDependencyIdList().contains(
                                        allocation2.getTimelineEntry().getTimelineProperty().getTimelineid())))
                .penalizeLong("checkDependencyId", HardSoftLongScore.ONE_HARD, (allocation, allocation2) -> 1L);
    }

    private Constraint checkPreviousStandstill(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isScored() && allocation.isFocused() && allocation.getIndex() > 1 &&
                        !locationRestrictionCheck(
                                allocation.getPreviousStandstill(),
                                allocation.getTimelineEntry().getHumanStateChange().getCurrentLocation()))
                .penalizeLong("checkPreviousStandstill", HardSoftLongScore.ONE_HARD, (allocation) -> 1L);
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************
}
