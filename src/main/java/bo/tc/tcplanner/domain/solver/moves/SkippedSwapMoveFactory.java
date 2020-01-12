package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;

import java.util.ArrayList;
import java.util.List;

public class SkippedSwapMoveFactory implements MoveListFactory<Schedule> {
    @Override
    public List<SkippedSwapMove> createMoveList(Schedule schedule) {
        List<SkippedSwapMove> moveList = new ArrayList<>();
        schedule.focusedAllocationSet.forEach(thisAllocation -> {
            schedule.getDummyAllocationIterator().forEachRemaining(dummyAllocation -> {
                moveList.add(new SkippedSwapMove(thisAllocation, dummyAllocation));
            });
        });

//        Collections.reverse(moveList);
        return moveList;
    }
}
