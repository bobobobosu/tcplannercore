package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.datastructure.TimelineEntry;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static bo.tc.tcplanner.domain.solver.filters.FilterTools.TimelineEntryCanChange;

public class PreciseTimeEntryMoveFactory implements MoveListFactory<Schedule> {

    @Override
    public List<SetValueMove> createMoveList(Schedule schedule) {
        List<Allocation> allocationList = schedule.getCondensedAllocationList();

        List<SetValueMove> moveList = new ArrayList<>();
        for (Allocation allocation : allocationList) {
            if (!TimelineEntryCanChange(allocation)) continue;
            for (TimelineEntry executionMode : allocation.getTimelineEntryRange()) {
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
