package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;

import java.util.ArrayList;
import java.util.List;

public class SkippedSwapMoveFactory implements MoveListFactory<Schedule> {
    @Override
    public List<SkippedSwapMove> createMoveList(Schedule schedule) {
        List<Allocation> dummyAllocationList = schedule.getDummyAllocationList();
        List<SkippedSwapMove> moveList = new ArrayList<>();
        for (Allocation thisAllocation : schedule.getFocusedAllocationList()) {
            for (Allocation dummyAllocation : dummyAllocationList) {
                moveList.add(new SkippedSwapMove(thisAllocation, dummyAllocation));
            }
        }
        return moveList;
    }
}
