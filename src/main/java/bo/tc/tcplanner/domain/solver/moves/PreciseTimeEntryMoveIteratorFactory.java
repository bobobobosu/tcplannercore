package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.datastructure.TimelineEntry;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveIteratorFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.*;

import static bo.tc.tcplanner.app.DroolsTools.locationRestrictionCheck;

public class PreciseTimeEntryMoveIteratorFactory implements MoveIteratorFactory<Schedule> {
    Map<String, List<TimelineEntry>> avareq2timelineEntryMap = new HashMap<>();

    @Override
    public long getSize(ScoreDirector<Schedule> scoreDirector) {
        return scoreDirector.getWorkingSolution().getAllocationList().size() * scoreDirector.getWorkingSolution().getAllocationList().get(0).getTimelineEntryRange().size() / 20;
    }

    @Override
    public Iterator<? extends Move<Schedule>> createOriginalMoveIterator(ScoreDirector<Schedule> scoreDirector) {
        return new Iterator<Move<Schedule>>() {
            Iterator<Allocation> dummyAllocationIterator = scoreDirector.getWorkingSolution().getDummyAllocationIterator();
            Allocation thisAllocation = null;
            List<TimelineEntry> matchingTimelineEntries = new ArrayList<>();

            @Override
            public boolean hasNext() {
                populateQueue();
                return matchingTimelineEntries.size() > 0;
            }

            private void populateQueue() {
                if (matchingTimelineEntries.size() > 0) return;
                while (matchingTimelineEntries.size() == 0) {
                    if (!dummyAllocationIterator.hasNext()) break;
                    thisAllocation = dummyAllocationIterator.next();
                    matchingTimelineEntries = new ArrayList<>(getMatchingTimelineEntries(thisAllocation));
                }
            }

            private List<TimelineEntry> getMatchingTimelineEntries(Allocation allocation) {
                Allocation nextAllocation = allocation.getNextAllocation();
                if (nextAllocation == null ||
                        locationRestrictionCheck(nextAllocation.getPreviousStandstill(),
                                nextAllocation.getTimelineEntry().getHumanStateChange().getCurrentLocation()))
                    return new ArrayList<>();
                String available = nextAllocation.getPreviousStandstill();
                String requirement = nextAllocation.getTimelineEntry().getHumanStateChange().getCurrentLocation();
                return avareq2timelineEntryMap.computeIfAbsent(available + requirement,
                        key -> {
                            List<TimelineEntry> timelineEntries = new ArrayList<>();
                            for (TimelineEntry timelineEntry : allocation.getSchedule().getTimelineEntryList()) {
                                if (locationRestrictionCheck(nextAllocation.getPreviousStandstill(),
                                        timelineEntry.getHumanStateChange().getCurrentLocation()) &&
                                        locationRestrictionCheck(timelineEntry.getHumanStateChange().getMovetoLocation(),
                                                nextAllocation.getTimelineEntry().getHumanStateChange().getCurrentLocation())) {
                                    timelineEntries.add(timelineEntry);
                                }
                            }
                            return timelineEntries;
                        });
            }

            @Override
            public Move<Schedule> next() {
                populateQueue();
                Allocation allocation = thisAllocation;
                TimelineEntry timelineEntry = matchingTimelineEntries.get(0);

                // advance
                matchingTimelineEntries.remove(0);

                return new SetValueMove(
                        Arrays.asList(allocation, allocation.getFocusedAllocationSet().higher(allocation)),
                        Arrays.asList(
                                new AllocationValues()
                                        .setExecutionMode(timelineEntry)
                                        .setProgressDelta(100)
                                        .setDelay(0),
                                new AllocationValues().setDelay(0))
                );
            }
        };
    }

    @Override
    public Iterator<? extends Move<Schedule>> createRandomMoveIterator(ScoreDirector<Schedule> scoreDirector, Random workingRandom) {
        return new Iterator<Move<Schedule>>() {
            Iterator<Allocation> dummyAllocationIterator = scoreDirector.getWorkingSolution().getDummyAllocationIterator();
            Allocation thisAllocation = null;
            List<TimelineEntry> matchingTimelineEntries = new ArrayList<>();

            @Override
            public boolean hasNext() {
                populateQueue();
                return matchingTimelineEntries.size() > 0;
            }

            private void populateQueue() {
                if (matchingTimelineEntries.size() > 0) return;
                while (matchingTimelineEntries.size() == 0) {
                    if (!dummyAllocationIterator.hasNext()) break;
                    thisAllocation = dummyAllocationIterator.next();
                    matchingTimelineEntries = new ArrayList<>(getMatchingTimelineEntries(thisAllocation));
                }
            }

            private List<TimelineEntry> getMatchingTimelineEntries(Allocation allocation) {
                Allocation nextAllocation = allocation.getNextAllocation();
                if (nextAllocation == null ||
                        locationRestrictionCheck(nextAllocation.getPreviousStandstill(),
                                nextAllocation.getTimelineEntry().getHumanStateChange().getCurrentLocation()))
                    return new ArrayList<>();
                String available = nextAllocation.getPreviousStandstill();
                String requirement = nextAllocation.getTimelineEntry().getHumanStateChange().getCurrentLocation();
                return avareq2timelineEntryMap.computeIfAbsent(available + requirement,
                        key -> {
                            List<TimelineEntry> timelineEntries = new ArrayList<>();
                            for (TimelineEntry timelineEntry : allocation.getSchedule().getTimelineEntryList()) {
                                if (locationRestrictionCheck(nextAllocation.getPreviousStandstill(),
                                        timelineEntry.getHumanStateChange().getCurrentLocation()) &&
                                        locationRestrictionCheck(timelineEntry.getHumanStateChange().getMovetoLocation(),
                                                nextAllocation.getTimelineEntry().getHumanStateChange().getCurrentLocation())) {
                                    timelineEntries.add(timelineEntry);
                                }
                            }
                            return timelineEntries;
                        });
            }

            @Override
            public Move<Schedule> next() {
                populateQueue();
                Allocation allocation = thisAllocation;
                TimelineEntry timelineEntry = matchingTimelineEntries.get(0);

                // advance
                matchingTimelineEntries.remove(0);

                return new SetValueMove(
                        Arrays.asList(allocation, allocation.getFocusedAllocationSet().higher(allocation)),
                        Arrays.asList(
                                new AllocationValues()
                                        .setExecutionMode(timelineEntry)
                                        .setProgressDelta(100)
                                        .setDelay(0),
                                new AllocationValues().setDelay(0))
                );
            }
        };
    }

}
