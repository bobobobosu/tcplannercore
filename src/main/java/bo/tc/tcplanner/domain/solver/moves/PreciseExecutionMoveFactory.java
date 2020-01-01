package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.ExecutionMode;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;

import java.util.*;

import static bo.tc.tcplanner.domain.solver.filters.FilterTools.*;

public class PreciseExecutionMoveFactory implements MoveListFactory<Schedule> {

    @Override
    public List<SetValueMove> createMoveList(Schedule schedule) {
        List<Allocation> allocationList = schedule.getDummyAllocationList();

        List<SetValueMove> moveList = new ArrayList<>();
        for (Allocation allocation : allocationList) {
            if (!ExecutionModeCanChange(allocation)) continue;
            for (ExecutionMode executionMode : allocation.getExecutionModeRange()) {
                moveList.add(new SetValueMove(
                        Arrays.asList(allocation),
                        Arrays.asList(new AllocationValues()
                                .setExecutionMode(executionMode)
                                .setProgressDelta(10))
                ));
            }
        }
        Collections.reverse(moveList);
        return moveList;

    }
}
