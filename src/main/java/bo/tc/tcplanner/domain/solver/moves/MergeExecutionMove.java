package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.ExecutionMode;
import bo.tc.tcplanner.domain.Schedule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static bo.tc.tcplanner.domain.solver.filters.FilterTools.*;

public class MergeExecutionMove extends AbstractMove<Schedule> {
    private Allocation allocation;
    private Allocation toAllocation;
    private AllocationValues oldallocationValues;
    private AllocationValues oldtoAllocationValues;

    public MergeExecutionMove(Allocation allocation, Allocation toAllocation) {
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
        Integer sum = allocation.getProgressdelta() + toAllocation.getProgressdelta();

        new AllocationValues()
                .setProgressDelta(sum)
                .apply(toAllocation, scoreDirector);
        new AllocationValues()
                .setExecutionMode(allocation.getSchedule().getExecutionModeList().get(0))
                .apply(allocation, scoreDirector);
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<Schedule> scoreDirector) {
        if (allocation.equals(toAllocation)) return false;
        if (isNotSplittable(allocation) || isNotSplittable(toAllocation)) return false;
        if (isNotInIndex(allocation) || isNotInIndex(toAllocation)) return false;
        if (isLocked(allocation) || isLocked(toAllocation)) return false;
        if (!allocation.isFocused() || !toAllocation.isFocused()) return false;
        return true;
    }

    @Override
    public MergeExecutionMove rebase(ScoreDirector<Schedule> destinationScoreDirector) {
        return new MergeExecutionMove(destinationScoreDirector.lookUpWorkingObject(allocation),
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
        return new ArrayList<>(Arrays.asList(toAllocation.getProgressdelta(), allocation.getProgressdelta(), allocation.getExecutionMode()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof MergeExecutionMove) {
            MergeExecutionMove other = (MergeExecutionMove) o;
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
