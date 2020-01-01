package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.ExecutionMode;
import bo.tc.tcplanner.domain.ExecutionModeType;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SplitExecutionMoveFactory implements MoveListFactory<Schedule> {
    @Override
    public List<SplitExecutionMove> createMoveList(Schedule schedule) {
        List<Allocation> dummyAllocationList = schedule.getDummyAllocationList();
        List<SplitExecutionMove> moveList = new ArrayList<>();

        for (Allocation thisAllocation : schedule.getFocusedAllocationList()) {
            for (Allocation dummyAllocation : dummyAllocationList) {
                for (ExecutionMode usableExecutionMode : thisAllocation.getSchedule().getExecutionModeList()) {
                    if (usableExecutionMode.getExecutionModeTypes().contains(ExecutionModeType.USABLE))
                        moveList.add(new SplitExecutionMove(thisAllocation, dummyAllocation, usableExecutionMode));
                }
            }
        }
        return moveList;
    }
}
