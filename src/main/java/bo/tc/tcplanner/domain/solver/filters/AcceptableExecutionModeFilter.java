package bo.tc.tcplanner.domain.solver.filters;

import bo.tc.tcplanner.domain.ExecutionMode;
import bo.tc.tcplanner.domain.Job;
import bo.tc.tcplanner.domain.JobType;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class AcceptableExecutionModeFilter implements SelectionFilter<Schedule, ExecutionMode> {
    @Override
    public boolean accept(ScoreDirector<Schedule> scoreDirector, ExecutionMode executionMode) {
        if (executionMode.getJob().getRownum() > 6000) return false;
        Job job = executionMode.getJob();
        return job.getJobType() == JobType.STANDARD;
    }
}
