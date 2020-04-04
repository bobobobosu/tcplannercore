package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.datastructure.LocationHierarchyMap;
import bo.tc.tcplanner.datastructure.TimelineEntry;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;

import java.util.*;

import static bo.tc.tcplanner.app.DroolsTools.locationRestrictionCheck;

public class PreciseTimeEntryMoveFactory implements MoveListFactory<Schedule> {
    Map<String, List<TimelineEntry>> avareq2timelineEntryMap = new HashMap<>();

    @Override
    public List<SetValueMove> createMoveList(Schedule schedule) {
        LocationHierarchyMap locationHierarchyMap = schedule.getLocationHierarchyMap();
        List<SetValueMove> moveList = new ArrayList<>();

        List<Allocation> dummyAllocationList = schedule.getDummyAllocationList();
        for (Allocation allocation : dummyAllocationList) {
            Allocation nextAllocation = allocation.getNextAllocation();
            if (nextAllocation == null ||
                    locationRestrictionCheck(locationHierarchyMap, nextAllocation.getPreviousStandstill(),
                            nextAllocation.getTimelineEntry().getHumanStateChange().getCurrentLocation())) continue;
            String available = nextAllocation.getPreviousStandstill();
            String requirement = nextAllocation.getTimelineEntry().getHumanStateChange().getCurrentLocation();
            List<TimelineEntry> matchTimelineEntries = avareq2timelineEntryMap.computeIfAbsent(available + requirement,
                    key -> {
                        List<TimelineEntry> timelineEntries = new ArrayList<>();
                        for (TimelineEntry timelineEntry : schedule.getTimelineEntryList()) {
                            if (locationRestrictionCheck(locationHierarchyMap, nextAllocation.getPreviousStandstill(),
                                    timelineEntry.getHumanStateChange().getCurrentLocation()) &&
                                    locationRestrictionCheck(locationHierarchyMap, timelineEntry.getHumanStateChange().getMovetoLocation(),
                                            nextAllocation.getTimelineEntry().getHumanStateChange().getCurrentLocation())) {
                                timelineEntries.add(timelineEntry);
                            }
                        }
                        return timelineEntries;
                    });

            for (TimelineEntry timelineEntry : matchTimelineEntries) {
                moveList.add(new SetValueMove(
                        Arrays.asList(allocation, allocation.getFocusedAllocationSet().higher(allocation)),
                        Arrays.asList(
                                new AllocationValues()
                                        .setExecutionMode(timelineEntry)
                                        .setProgressDelta(100)
                                        .setDelay(0),
                                new AllocationValues().setDelay(0))
                ));
            }
        }
        return moveList;

    }
}
