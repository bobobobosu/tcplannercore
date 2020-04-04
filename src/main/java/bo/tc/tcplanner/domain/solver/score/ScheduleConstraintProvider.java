package bo.tc.tcplanner.domain.solver.score;

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.domain.Allocation;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

import java.time.Duration;
import java.util.function.Function;

import static bo.tc.tcplanner.app.DroolsTools.locationRestrictionCheck;
import static org.optaplanner.core.api.score.stream.Joiners.equal;

public class ScheduleConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                checkDependencyId(constraintFactory),
                checkTimeOverlapping(constraintFactory),
                checkDeadline(constraintFactory),
                checkAliveline(constraintFactory),
                checkScheduleAfter(constraintFactory),
                checkPreviousStandstill(constraintFactory),
                checkRequirementsDeficit(constraintFactory),
                checkCapacityRequirements(constraintFactory),
                checkSplittable(constraintFactory),
                timeRequirement(constraintFactory),
                dummyJob(constraintFactory),
                checkExcessResource(constraintFactory),
                timeAdvisory(constraintFactory),
                punishFragmentation(constraintFactory),
                laterTheBetter(constraintFactory),
                earlierTheBetter(constraintFactory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************
    private Constraint checkDependencyId(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isScored() && allocation.isFocused())
                .join(Integer.class).filter(((allocation, integer) ->
                        allocation.getTimelineEntry().getTimelineProperty().getDependencyIdList().contains(integer)))
                .join(factory.from(Allocation.class).filter(Allocation::isFocused),
                        equal(((a, b) -> b),
                                (allocation -> allocation.getTimelineEntry().getTimelineProperty().getTimelineid())))

                .penalizeLong("checkDependencyId", HardMediumSoftLongScore.ONE_HARD,
                        (a, b, c) -> 100);

    }

    private Constraint checkTimeOverlapping(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isScored() && allocation.isFocused() &&
                        allocation.getPredecessorsDoneDate().isAfter(allocation.getStartDate()))
                .penalizeLong("checkTimeOverlapping", HardMediumSoftLongScore.ONE_HARD,
                        (a -> (long) (100 * ((double) Duration.between(
                                a.getStartDate(),
                                a.getPredecessorsDoneDate()).toMinutes()))));

    }

    private Constraint checkDeadline(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isScored() && allocation.isFocused() &&
                        allocation.getTimelineEntry().getChronoProperty().getZonedDeadline().isBefore(allocation.getEndDate()))
                .penalizeLong("checkDeadline", HardMediumSoftLongScore.ONE_HARD,
                        (a -> (long) (100 * ((double) Duration.between(
                                a.getTimelineEntry().getChronoProperty().getZonedDeadline(),
                                a.getEndDate()).toMinutes()))));

    }

    private Constraint checkAliveline(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isScored() && allocation.isFocused() &&
                        allocation.getTimelineEntry().getChronoProperty().getZonedAliveline().isAfter(allocation.getStartDate()))
                .penalizeLong("checkAliveline", HardMediumSoftLongScore.ONE_HARD,
                        (a -> (long) (100 * ((double) Duration.between(
                                a.getStartDate(),
                                a.getTimelineEntry().getChronoProperty().getZonedAliveline()
                        ).toMinutes()))));
    }

    private Constraint checkScheduleAfter(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isScored() && allocation.isFocused() &&
                        allocation.getTimelineEntry().getTimelineProperty().getPlanningWindowType().equals(
                                PropertyConstants.PlanningWindowTypes.types.Draft.name()) &&
                        allocation.getStartDate().isBefore(
                                allocation.getSchedule().getProblemTimelineBlock().getZonedBlockScheduleAfter()))
                .penalizeLong("checkScheduleAfter", HardMediumSoftLongScore.ONE_HARD,
                        (a -> (long) (((double) Duration.between(
                                a.getStartDate(),
                                a.getSchedule().getProblemTimelineBlock().getZonedBlockScheduleAfter()
                        ).toMinutes() / 10))));
    }

    private Constraint checkPreviousStandstill(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isScored() && allocation.isFocused() && allocation.getIndex() > 1 &&
                        !locationRestrictionCheck(
                                allocation.getSchedule().getLocationHierarchyMap(),
                                allocation.getPreviousStandstill(),
                                allocation.getTimelineEntry().getHumanStateChange().getCurrentLocation()))
                .penalizeLong("checkPreviousStandstill",
                        HardMediumSoftLongScore.ONE_HARD, (allocation) -> 100L);
    }

    private Constraint checkRequirementsDeficit(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isScored() && allocation.isFocused())
                .penalizeLong("checkRequirementsDeficit", HardMediumSoftLongScore.ONE_HARD,
                        (allocation) -> -Math.round(allocation.getResourceElementMapDeficitScore() * 100));
    }

    private Constraint checkCapacityRequirements(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isScored() && allocation.isFocused())
                .penalizeLong("checkCapacityRequirements", HardMediumSoftLongScore.ONE_HARD,
                        (allocation) -> -Math.round(allocation.getResourceElementMapExcessScore() * 100));
    }

    private Constraint checkSplittable(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isScored() && allocation.isFocused() &&
                        allocation.getTimelineEntry().getChronoProperty().getSplittable() == 0 &&
                        allocation.getProgressdelta() !=
                                allocation.getTimelineEntry().getProgressChange().getProgressDelta() * 100)
                .penalizeLong("checkSplittable", HardMediumSoftLongScore.ONE_HARD,
                        (a ->
                                10 * (int) Math.abs(a.getProgressdelta() -
                                        a.getTimelineEntry().getProgressChange().getProgressDelta() * 100)));
    }

    private Constraint timeRequirement(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isScored() && allocation.isFocused() &&
                        allocation.getRequirementTimerangeMatch())
                .join(Long.class, equal(Allocation::getRequirementTimerangeScore, Function.identity()))
                .filter((allocation, b) -> b < 5)
                .penalizeLong("timeRequirement", HardMediumSoftLongScore.ONE_HARD,
                        ((allocation, aLong) -> 100 * aLong));
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    private Constraint dummyJob(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isScored() && allocation.isFocused() &&
                        allocation.getTimelineEntry().getTimelineProperty().getPlanningWindowType()
                                .equals(PropertyConstants.PlanningWindowTypes.types.Draft.name()))
                .penalizeLong("dummyJob", HardMediumSoftLongScore.ONE_MEDIUM,
                        a -> 1000);
    }

    private Constraint checkExcessResource(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isScored() && allocation.isFocused())
                .join(Double.class, equal(Allocation::getResourceElementMapExcessScore, Function.identity()))
                .filter((allocation, b) -> b < 0)
                .penalizeLong("checkExcessResource", HardMediumSoftLongScore.ONE_MEDIUM,
                        ((allocation, aLong) -> (long) (-100 * aLong)));
    }

    private Constraint timeAdvisory(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isScored() && allocation.isFocused() &&
                        allocation.getAdviceTimerangeMatch())
                .join(Long.class, equal(Allocation::getAdviceTimerangeScore, Function.identity()))
                .filter((allocation, b) -> b < -5)
                .penalizeLong("timeAdvisory", HardMediumSoftLongScore.ONE_MEDIUM,
                        ((allocation, aLong) -> -100 * aLong));
    }

    // ############################################################################
    // Soft constraints
    // ############################################################################

    private Constraint punishFragmentation(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isScored() && allocation.isFocused() &&
                        allocation.getProgressdelta() < 100)
                .penalizeLong("punishFragmentation", HardMediumSoftLongScore.ONE_SOFT,
                        (allocation) ->
                                -(allocation.getProgressdelta() < 50 ?
                                        -allocation.getProgressdelta() :
                                        allocation.getProgressdelta() - 100));
    }

    private Constraint laterTheBetter(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isScored() && allocation.isFocused() &&
                        allocation.getTimelineEntry().getChronoProperty().getGravity() == 1)
                .rewardLong("laterTheBetter", HardMediumSoftLongScore.ONE_SOFT,
                        (a -> (Duration.between(a.getSchedule().getProblemTimelineBlock().getZonedBlockStartTime(),
                                a.getStartDate())).toMinutes()));
    }

    private Constraint earlierTheBetter(ConstraintFactory factory) {
        return factory.from(Allocation.class)
                .filter(allocation -> allocation.isScored() && allocation.isFocused() &&
                        allocation.getTimelineEntry().getChronoProperty().getGravity() == -1)
                .penalizeLong("earlierTheBetter", HardMediumSoftLongScore.ONE_SOFT,
                        (a -> (Duration.between(a.getSchedule().getProblemTimelineBlock().getZonedBlockStartTime(),
                                a.getStartDate())).toMinutes()));
    }
}
