package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static bo.tc.tcplanner.domain.solver.filters.FilterTools.*;

public class MergeTimelineEntryMove extends AbstractMove<Schedule> {
    private Allocation allocation;
    private Allocation toAllocation;
    private AllocationValues oldallocationValues;
    private AllocationValues oldtoAllocationValues;

    public MergeTimelineEntryMove(Allocation allocation, Allocation toAllocation) {
        this.allocation = allocation;
        this.toAllocation = toAllocation;
        this.oldallocationValues = new AllocationValues().extract(allocation);
        this.oldtoAllocationValues = new AllocationValues().extract(toAllocation);
    }

    @Override
    protected AbstractMove<Schedule> createUndoMove(ScoreDirector<Schedule> scoreDirector) {
        return new SetValueMove(
                Arrays.asList(allocation, toAllocation),
                Arrays.asList(oldallocationValues, oldtoAllocationValues));
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Schedule> scoreDirector) {
        int sum = allocation.getProgressdelta() + toAllocation.getProgressdelta();
        int mergedToProgressDelta = Math.max(sum, 100);
        int rest = sum - mergedToProgressDelta;

        new AllocationValues()
                .setProgressDelta(sum)
                .apply(toAllocation, scoreDirector);
        new AllocationValues()
                .setProgressDelta(rest)
                .setExecutionMode(rest == 0 ?
                        allocation.getSchedule().special.dummyTimelineEntry : allocation.getTimelineEntry())
                .apply(allocation, scoreDirector);
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<Schedule> scoreDirector) {
        if (!IsFocused(allocation)) return false;
        if (!IsFocused(toAllocation)) return false;
        if (!TimelineEntryCanChange(allocation)) return false;
        if (!ProgressDeltaCanChange(toAllocation)) return false;
        return !allocation.equals(toAllocation);
    }

    @Override
    public MergeTimelineEntryMove rebase(ScoreDirector<Schedule> destinationScoreDirector) {
        return new MergeTimelineEntryMove(destinationScoreDirector.lookUpWorkingObject(allocation),
                destinationScoreDirector.lookUpWorkingObject(toAllocation));
    }


    public String toString() {
        return "{" + allocation + " + " + toAllocation + "}" + " -> " + " {" + toAllocation + "}";
    }

    @Override
    public String getSimpleMoveTypeDescription() {
        return "MergeExecutionMove(Allocation.executionMode,Allocation.progressDelta)";
    }

    @Override
    public Collection<? extends Object> getPlanningEntities() {
        return new ArrayList<>(Arrays.asList(toAllocation, allocation));
    }

    @Override
    public Collection<? extends Object> getPlanningValues() {
        return new ArrayList<>(Arrays.asList(toAllocation.getProgressdelta(), allocation.getProgressdelta(), allocation.getTimelineEntry()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof MergeTimelineEntryMove) {
            MergeTimelineEntryMove other = (MergeTimelineEntryMove) o;
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
