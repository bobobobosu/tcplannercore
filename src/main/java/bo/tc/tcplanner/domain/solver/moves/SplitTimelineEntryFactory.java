package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.datastructure.TimelineEntry;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;

import java.util.ArrayList;
import java.util.List;

public class SplitTimelineEntryFactory implements MoveListFactory<Schedule> {
    @Override
    public List<SplitTimelineEntryMove> createMoveList(Schedule schedule) {
        List<Allocation> dummyAllocationList = schedule.getDummyAllocationList();
        List<SplitTimelineEntryMove> moveList = new ArrayList<>();

        for (Allocation thisAllocation : schedule.getFocusedAllocationList()) {
            for (Allocation dummyAllocation : dummyAllocationList) {
                moveList.add(new SplitTimelineEntryMove(thisAllocation, dummyAllocation,
                        thisAllocation.getSchedule().getJob2jobcloneMap().get(thisAllocation.getTimelineEntry())));
            }
        }
        return moveList;
    }
}
