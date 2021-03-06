package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;

import java.util.ArrayList;
import java.util.List;

public class SplitTimelineEntryFactory implements MoveListFactory<Schedule> {
    @Override
    public List<SplitTimelineEntryMove> createMoveList(Schedule schedule) {
        List<SplitTimelineEntryMove> moveList = new ArrayList<>();
        schedule.focusedAllocationSet.forEach(thisAllocation -> {
            schedule.getDummyAllocationIterator().forEachRemaining(dummyAllocation -> {
                moveList.add(new SplitTimelineEntryMove(thisAllocation, dummyAllocation,
                        thisAllocation.getSchedule().getJob2jobcloneMap().get(thisAllocation.getTimelineEntry())));
            });
        });
//        Collections.reverse(moveList);
        return moveList;
    }
}
