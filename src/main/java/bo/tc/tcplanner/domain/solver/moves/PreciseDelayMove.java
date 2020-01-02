package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import bo.tc.tcplanner.domain.solver.filters.DelayCanChangeFilter;
import bo.tc.tcplanner.domain.solver.filters.IsFocusedFilter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.Arrays;
import java.util.Collection;

import static bo.tc.tcplanner.domain.solver.filters.FilterTools.*;

public class PreciseDelayMove extends AbstractMove<Schedule> {
    private Integer toDelay;
    private Allocation allocation;
    private Allocation nextAllocation;

    public PreciseDelayMove(Allocation allocation, Integer toDelay) {
        this.toDelay = toDelay;
        this.allocation = allocation;
        this.nextAllocation = allocation.getNextFocusedAllocation();

    }

    @Override
    protected AbstractMove createUndoMove(ScoreDirector scoreDirector) {
        return new PreciseDelayMove(allocation, allocation.getDelay());
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector scoreDirector) {
        Integer sum = allocation.getDelay() + nextAllocation.getDelay();

        scoreDirector.beforeVariableChanged(allocation, "delay");
        allocation.setDelay(toDelay);
        scoreDirector.afterVariableChanged(allocation, "delay");

        scoreDirector.beforeVariableChanged(nextAllocation, "delay");
        nextAllocation.setDelay(sum - toDelay);
        scoreDirector.afterVariableChanged(nextAllocation, "delay");
    }

    @Override
    public boolean isMoveDoable(ScoreDirector scoreDirector) {
        if (!IsFocused(allocation)) return false;
        if (!DelayCanChange(allocation)) return false;
        return (!allocation.getDelay().equals(this.toDelay)) &&
                (allocation.getNextFocusedAllocation() != null);
    }

    @Override
    public PreciseDelayMove rebase(ScoreDirector<Schedule> destinationScoreDirector) {
        return new PreciseDelayMove(destinationScoreDirector.lookUpWorkingObject(allocation),
                destinationScoreDirector.lookUpWorkingObject(toDelay));
    }

    public String toString() {
        return "PreciseDelay "+allocation + " {" + allocation.getDelay() + " -> " + toDelay + "}";
    }

    @Override
    public String getSimpleMoveTypeDescription() {
        return "PreciseDelayMove(Allocation.delay)";
    }

    @Override
    public Collection<? extends Object> getPlanningEntities() {
        return Arrays.asList(allocation, nextAllocation);
    }

    @Override
    public Collection<? extends Object> getPlanningValues() {
        return Arrays.asList(allocation.getDelay(), nextAllocation.getDelay());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof PreciseDelayMove) {
            PreciseDelayMove other = (PreciseDelayMove) o;
            return new EqualsBuilder()
                    .append(allocation, other.allocation)
                    .append(toDelay, other.toDelay)
                    .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(allocation)
                .append(toDelay)
                .toHashCode();
    }

}
