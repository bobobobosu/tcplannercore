package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.ExecutionMode;
import bo.tc.tcplanner.domain.Schedule;
import bo.tc.tcplanner.domain.solver.filters.IsFocusedFilter;
import bo.tc.tcplanner.domain.solver.filters.ProgressDeltaCanChangeFilter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;

import static bo.tc.tcplanner.domain.solver.filters.FilterTools.*;

public class SplitExecutionMove extends AbstractMove<Schedule> {
    private Allocation allocation;
    private Allocation dummyAllocation;
    private AllocationValues oldallocationValues;
    private AllocationValues olddummyAllocationValues;
    private ExecutionMode usableExecutionMode;

    public SplitExecutionMove(Allocation allocation, Allocation dummyAllocation, ExecutionMode usableExecutionMode) {
        this.allocation = allocation;
        this.dummyAllocation = dummyAllocation;
        this.oldallocationValues = new AllocationValues().extract(allocation);
        this.olddummyAllocationValues = new AllocationValues().extract(dummyAllocation);
        this.usableExecutionMode = usableExecutionMode;
    }

    @Override
    protected AbstractMove<Schedule> createUndoMove(ScoreDirector<Schedule> scoreDirector) {
        return new SetValueMove(
                Arrays.asList(allocation, dummyAllocation),
                Arrays.asList(oldallocationValues, olddummyAllocationValues));
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Schedule> scoreDirector) {
        Integer sum = allocation.getProgressdelta();
        int max = sum / 2;

        ZonedDateTime thisStart = allocation.getStartDate();
        ZonedDateTime nextStart = allocation.getNextFocusedAllocation().getStartDate();
        if (allocation.getExecutionMode().getHumanStateChange().getDuration() > 0 &&
                allocation.getEndDate().isAfter(nextStart)) {
            max = (int) (100 * allocation.getExecutionMode().getProgressChange().getProgressDelta() * (
                    Duration.between(thisStart, nextStart).toMinutes() /
                            allocation.getExecutionMode().getHumanStateChange().getDuration()));
        }

        new AllocationValues()
                .setProgressDelta(max)
                .setExecutionMode(usableExecutionMode)
                .apply(dummyAllocation, scoreDirector);

        new AllocationValues()
                .setProgressDelta(sum - dummyAllocation.getProgressdelta())
                .apply(allocation, scoreDirector);
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<Schedule> scoreDirector) {
        if (!IsFocused(allocation)) return false;
        if (!ProgressDeltaCanChange(allocation)) return false;
        if (!dummyAllocation.getExecutionMode().equals(allocation.getSchedule().special.dummyExecutionMode))
            return false;
        return !allocation.equals(dummyAllocation);
    }

    @Override
    public SplitExecutionMove rebase(ScoreDirector<Schedule> destinationScoreDirector) {
        return new SplitExecutionMove(
                destinationScoreDirector.lookUpWorkingObject(allocation),
                destinationScoreDirector.lookUpWorkingObject(dummyAllocation),
                destinationScoreDirector.lookUpWorkingObject(usableExecutionMode));
    }


    public String toString() {
        return "{" + allocation + "}" + " -> " + " {" + allocation + " + " + dummyAllocation + "}";
    }

    @Override
    public String getSimpleMoveTypeDescription() {
        return "SplitExecutionMove(Allocation.executionMode,Allocation.progressDelta)";
    }

    @Override
    public Collection<? extends Object> getPlanningEntities() {
        return Arrays.asList(allocation, dummyAllocation);
    }

    @Override
    public Collection<? extends Object> getPlanningValues() {
        return Arrays.asList(allocation.getProgressdelta(), dummyAllocation.getProgressdelta(), dummyAllocation.getExecutionMode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof SplitExecutionMove) {
            SplitExecutionMove other = (SplitExecutionMove) o;
            return new EqualsBuilder()
                    .append(allocation, other.allocation)
                    .append(dummyAllocation, other.dummyAllocation)
                    .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(allocation)
                .append(dummyAllocation)
                .toHashCode();
    }

}
