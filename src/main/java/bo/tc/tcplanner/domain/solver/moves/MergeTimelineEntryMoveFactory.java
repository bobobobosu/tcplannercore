package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.datastructure.TimelineEntry;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MergeTimelineEntryMoveFactory implements MoveListFactory<Schedule> {
    @Override
    public List<MergeTimelineEntryMove> createMoveList(Schedule schedule) {
        Map<TimelineEntry, List<Allocation>> map = schedule
                .getAllocationList()
                .stream()
                .collect(Collectors.groupingBy(Allocation::getTimelineEntry));

        List<MergeTimelineEntryMove> moveList = new ArrayList<>();
        for (Allocation thisAllocation : schedule.getFocusedAllocationList()) {
            if (map.containsKey(thisAllocation.getTimelineEntry())) {
                for (Allocation toAllocation : map.get(thisAllocation.getTimelineEntry())) {
                    moveList.add(new MergeTimelineEntryMove(thisAllocation, toAllocation));
                }
            }
        }

//        Collections.reverse(moveList);
        return moveList;
    }
}

