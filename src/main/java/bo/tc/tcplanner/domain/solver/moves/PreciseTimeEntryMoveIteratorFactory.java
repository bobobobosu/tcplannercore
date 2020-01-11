package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.datastructure.TimelineEntry;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveIteratorFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import static bo.tc.tcplanner.domain.solver.filters.FilterTools.TimelineEntryCanChange;

public class PreciseTimeEntryMoveIteratorFactory implements MoveIteratorFactory<Schedule> {

    @Override
    public long getSize(ScoreDirector<Schedule> scoreDirector) {
        return scoreDirector.getWorkingSolution().getAllocationList().size() * scoreDirector.getWorkingSolution().getAllocationList().get(0).getTimelineEntryRange().size() / 20;
    }

    @Override
    public Iterator<? extends Move<Schedule>> createOriginalMoveIterator(ScoreDirector<Schedule> scoreDirector) {

        return new Iterator<Move<Schedule>>() {
            Allocation thisAllocation = scoreDirector.getWorkingSolution().getAllocationList().get(0);
            Iterator<TimelineEntry> thisTimelineEntry = scoreDirector.getWorkingSolution().getAllocationList().get(0).getTimelineEntryRange().listIterator();

            @Override
            public boolean hasNext() {
                return thisAllocation != null && thisTimelineEntry.hasNext();
            }

            @Override
            public Move<Schedule> next() {
                Allocation saveAllocation = thisAllocation;
                TimelineEntry saveTimeEntry = thisTimelineEntry.next();
                if (!thisTimelineEntry.hasNext()) {
                    do {
                        thisAllocation = thisAllocation.getNextFocusedAllocation();
                        thisTimelineEntry = thisAllocation.getTimelineEntryRange().listIterator();
                    } while (!TimelineEntryCanChange(thisAllocation));
                }
                return new SetValueMove(
                        Arrays.asList(saveAllocation, saveAllocation.getNextFocusedAllocation()),
                        Arrays.asList(
                                new AllocationValues()
                                        .setExecutionMode(saveTimeEntry)
                                        .setProgressDelta(100)
                                        .setDelay(0),
                                new AllocationValues().setDelay(0))
                );
            }
        };
    }

    @Override
    public Iterator<? extends Move<Schedule>> createRandomMoveIterator(ScoreDirector<Schedule> scoreDirector, Random random) {
        return new Iterator<Move<Schedule>>() {
            Allocation thisAllocation = scoreDirector.getWorkingSolution().getAllocationList().get(0);
            Iterator<TimelineEntry> thisTimelineEntry = scoreDirector.getWorkingSolution().getAllocationList().get(0).getTimelineEntryRange().listIterator();

            @Override
            public boolean hasNext() {
                return thisAllocation != null && thisTimelineEntry.hasNext();
            }

            @Override
            public Move<Schedule> next() {
                Allocation saveAllocation = thisAllocation;
                TimelineEntry saveTimeEntry = thisTimelineEntry.next();
                if (!thisTimelineEntry.hasNext()) {
                    do {
                        thisAllocation = thisAllocation.getNextFocusedAllocation();
                        thisTimelineEntry = thisAllocation.getTimelineEntryRange().listIterator();
                    } while (!TimelineEntryCanChange(thisAllocation));
                }
                return new SetValueMove(
                        Arrays.asList(saveAllocation, saveAllocation.getNextFocusedAllocation()),
                        Arrays.asList(
                                new AllocationValues()
                                        .setExecutionMode(saveTimeEntry)
                                        .setProgressDelta(100)
                                        .setDelay(0),
                                new AllocationValues().setDelay(0))
                );
            }
        };
    }
}
