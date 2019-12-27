package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.ExecutionMode;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class AllocationValues {
    private ExecutionMode executionMode = null;
    private Integer progressDelta = null;
    private Integer delay = null;

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    public AllocationValues setExecutionMode(ExecutionMode executionMode) {
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
                .setExecutionMode(allocation.getExecutionMode())
                .setDelay(allocation.getDelay());
    }

    public void apply(Allocation allocation, ScoreDirector scoreDirector) {
        if (progressDelta != null) {
            scoreDirector.beforeVariableChanged(allocation, "progressdelta");
            allocation.setProgressdelta(progressDelta);
            scoreDirector.afterVariableChanged(allocation, "progressdelta");
        }
        if (executionMode != null) {
            scoreDirector.beforeVariableChanged(allocation, "executionMode");
            allocation.setExecutionMode(executionMode);
            scoreDirector.afterVariableChanged(allocation, "executionMode");
        }
        if (delay != null) {
            scoreDirector.beforeVariableChanged(allocation, "delay");
            allocation.setDelay(delay);
            scoreDirector.afterVariableChanged(allocation, "delay");
        }
    }
}
