package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.datastructure.TimelineEntry;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveIteratorFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Random;

public class PreciseTimeEntryMoveIteratorFactory implements MoveIteratorFactory<Schedule> {

    @Override
    public long getSize(ScoreDirector<Schedule> scoreDirector) {
        return scoreDirector.getWorkingSolution().getAllocationList().size() * scoreDirector.getWorkingSolution().getAllocationList().get(0).getTimelineEntryRange().size() / 20;
    }

    @Override
    public Iterator<? extends Move<Schedule>> createOriginalMoveIterator(ScoreDirector<Schedule> scoreDirector) {
        return new Iterator<Move<Schedule>>() {
            Iterator<Allocation> thisFocusedAllocationIterator = scoreDirector.getWorkingSolution().focusedAllocationSet.iterator();
            Allocation thisAllocation = thisFocusedAllocationIterator.next();
            ListIterator<TimelineEntry> timelineEntryIterator = thisAllocation.getTimelineEntryRange().listIterator();

            @Override
            public boolean hasNext() {
                return thisAllocation != null;
            }

            @Override
            public Move<Schedule> next() {
                if (!timelineEntryIterator.hasNext()) {
                    thisAllocation = thisFocusedAllocationIterator.hasNext() ? thisFocusedAllocationIterator.next() : null;
                    timelineEntryIterator = scoreDirector.getWorkingSolution().getTimelineEntryList().listIterator();
                }
                return new SetValueMove(
                        Arrays.asList(thisAllocation,
                                scoreDirector.getWorkingSolution().focusedAllocationSet.higher(thisAllocation)),
                        Arrays.asList(
                                new AllocationValues()
                                        .setExecutionMode(timelineEntryIterator.next())
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
            Iterator<Allocation> thisFocusedAllocationIterator = scoreDirector.getWorkingSolution().focusedAllocationSet.iterator();
            Allocation thisAllocation = thisFocusedAllocationIterator.next();
            ListIterator<TimelineEntry> timelineEntryIterator = thisAllocation.getTimelineEntryRange().listIterator();

            @Override
            public boolean hasNext() {
                return thisAllocation != null;
            }

            @Override
            public Move<Schedule> next() {
                if (!timelineEntryIterator.hasNext()) {
                    thisAllocation = thisFocusedAllocationIterator.hasNext() ? thisFocusedAllocationIterator.next() : null;
                    timelineEntryIterator = scoreDirector.getWorkingSolution().getTimelineEntryList().listIterator();
                }
                return new SetValueMove(
                        Arrays.asList(thisAllocation,
                                scoreDirector.getWorkingSolution().focusedAllocationSet.higher(thisAllocation)),
                        Arrays.asList(
                                new AllocationValues()
                                        .setExecutionMode(timelineEntryIterator.next())
                                        .setProgressDelta(100)
                                        .setDelay(0),
                                new AllocationValues().setDelay(0))
                );
            }
        };
    }
}
