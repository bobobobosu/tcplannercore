package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;

import java.util.ArrayList;
import java.util.List;

public class MergeTimelineEntryMoveFactory implements MoveListFactory<Schedule> {
    @Override
    public List<MergeTimelineEntryMove> createMoveList(Schedule schedule) {
        List<MergeTimelineEntryMove> moveList = new ArrayList<>();
        for (Allocation thisAllocation : schedule.getFocusedAllocationList()) {
            for (Allocation toAllocation : thisAllocation.getTimelineEntry().getAllocationList()) {
                moveList.add(new MergeTimelineEntryMove(thisAllocation, toAllocation));
            }
        }

//        Collections.reverse(moveList);
        return moveList;
    }
}

