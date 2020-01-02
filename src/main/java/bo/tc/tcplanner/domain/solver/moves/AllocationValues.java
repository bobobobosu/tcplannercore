package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.datastructure.TimelineEntry;
import bo.tc.tcplanner.domain.Allocation;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class AllocationValues {
    private TimelineEntry executionMode = null;
    private Integer progressDelta = null;
    private Integer delay = null;

    public TimelineEntry getExecutionMode() {
        return executionMode;
    }

    public AllocationValues setExecutionMode(TimelineEntry executionMode) {
        this.executionMode = executionMode;
        return this;
    }

    public Integer getProgressDelta() {
        return progressDelta;
    }

    public AllocationValues setProgressDelta(Integer progressDelta) {
        this.progressDelta = progressDelta;
        return this;
    }

    public Integer getDelay() {
        return delay;
    }

    public AllocationValues setDelay(Integer delay) {
        this.delay = delay;
        return this;
    }

    public AllocationValues extract(Allocation allocation) {
        return this
                .setProgressDelta(allocation.getProgressdelta())
                .setExecutionMode(allocation.getTimelineEntry())
                .setDelay(allocation.getDelay());
    }

    public void apply(Allocation allocation, ScoreDirector scoreDirector) {
        if (progressDelta != null) {
            scoreDirector.beforeVariableChanged(allocation, "progressdelta");
            allocation.setProgressdelta(progressDelta);
            scoreDirector.afterVariableChanged(allocation, "progressdelta");
        }
        if (executionMode != null) {
            scoreDirector.beforeVariableChanged(allocation, "timelineEntry");
            allocation.setTimelineEntry(executionMode);
            scoreDirector.afterVariableChanged(allocation, "timelineEntry");
        }
        if (delay != null) {
            scoreDirector.beforeVariableChanged(allocation, "delay");
            allocation.setDelay(delay);
            scoreDirector.afterVariableChanged(allocation, "delay");
        }
    }
}
