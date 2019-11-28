package bo.tc.tcplanner.domain.solver.filters;

import bo.tc.tcplanner.datastructure.converters.DataStructureBuilder;
import bo.tc.tcplanner.domain.ExecutionMode;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class DummyExecutionModeFilter implements SelectionFilter<Schedule, ExecutionMode> {
    @Override
    public boolean accept(ScoreDirector<Schedule> scoreDirector, ExecutionMode executionMode) {
        return executionMode.getJob().equals(DataStructureBuilder.dummyJob);
    }
}
