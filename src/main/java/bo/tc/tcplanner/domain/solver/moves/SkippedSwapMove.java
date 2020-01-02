package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.Arrays;
import java.util.Collection;

import static bo.tc.tcplanner.domain.solver.filters.FilterTools.TimelineEntryCanChange;

public class SkippedSwapMove extends AbstractMove<Schedule> {
    private Allocation allocation;
    private Allocation toAllocation;

    public SkippedSwapMove(Allocation allocation, Allocation toAllocation) {
        this.allocation = allocation;
        this.toAllocation = toAllocation;
    }

    @Override
    protected AbstractMove<Schedule> createUndoMove(ScoreDirector<Schedule> scoreDirector) {
        return new SkippedSwapMove(allocation, toAllocation);
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Schedule> scoreDirector) {
        AllocationValues oldallocationValues = new AllocationValues().extract(allocation);
        AllocationValues oldtoAllocationValues = new AllocationValues().extract(toAllocation);
        oldallocationValues.apply(toAllocation, scoreDirector);
        oldtoAllocationValues.apply(allocation, scoreDirector);
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<Schedule> scoreDirector) {
        if (!TimelineEntryCanChange(allocation)) return false;
        if (!TimelineEntryCanChange(toAllocation)) return false;
        return !allocation.equals(toAllocation);
    }

    @Override
    public SkippedSwapMove rebase(ScoreDirector<Schedule> destinationScoreDirector) {
        return new SkippedSwapMove(destinationScoreDirector.lookUpWorkingObject(allocation),
                destinationScoreDirector.lookUpWorkingObject(toAllocation));
    }


    public String toString() {
        return "SkippedSwap {" + allocation + " <-> " + toAllocation + "}";
    }

    @Override
    public String getSimpleMoveTypeDescription() {
        return "SkippedSwapMove(Allocation,Allocation)";
    }

    @Override
    public Collection<? extends Object> getPlanningEntities() {
        return Arrays.asList(toAllocation, allocation);
    }

    @Override
    public Collection<? extends Object> getPlanningValues() {
        return Arrays.asList(
                allocation.getProgressdelta(), allocation.getTimelineEntry(), allocation.getDelay(),
                toAllocation.getProgressdelta(), toAllocation.getTimelineEntry(), toAllocation.getDelay());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof SkippedSwapMove) {
            SkippedSwapMove other = (SkippedSwapMove) o;
            return new EqualsBuilder()
                    .append(allocation, other.allocation)
                    .append(toAllocation, other.toAllocation)
                    .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(allocation)
                .append(toAllocation)
                .toHashCode();
    }
}
