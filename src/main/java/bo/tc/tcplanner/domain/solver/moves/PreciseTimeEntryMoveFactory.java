package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.datastructure.TimelineEntry;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static bo.tc.tcplanner.domain.solver.filters.FilterTools.TimelineEntryCanChange;

public class PreciseTimeEntryMoveFactory implements MoveListFactory<Schedule> {

    @Override
    public List<SetValueMove> createMoveList(Schedule schedule) {
        List<Allocation> allocationList = schedule.getCondensedAllocationList();

        List<SetValueMove> moveList = new ArrayList<>();
        for (Allocation allocation : allocationList) {
            if (!TimelineEntryCanChange(allocation)) continue;
            for (TimelineEntry timelineEntry : allocation.getTimelineEntryRange()) {
                if (timelineEntry.equals(allocation.getTimelineEntry())) continue;
                moveList.add(new SetValueMove(
                        Arrays.asList(allocation, allocation.getNextFocusedAllocation()),
                        Arrays.asList(
                                new AllocationValues()
                                        .setExecutionMode(timelineEntry)
                                        .setProgressDelta(100)
                                        .setDelay(0),
                                new AllocationValues().setDelay(0))
                ));
            }
        }
//        Collections.reverse(moveList);
        return moveList;

    }
}
