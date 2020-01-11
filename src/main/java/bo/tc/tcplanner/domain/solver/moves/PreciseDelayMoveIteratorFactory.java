package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveIteratorFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.Iterator;
import java.util.Random;

import static bo.tc.tcplanner.domain.solver.filters.FilterTools.DelayCanChange;

public class PreciseDelayMoveIteratorFactory implements MoveIteratorFactory<Schedule> {
    @Override
    public long getSize(ScoreDirector<Schedule> scoreDirector) {
        Schedule thisSchedule = scoreDirector.getWorkingSolution();
        return (thisSchedule.getAllocationList().size() / 20) *
                thisSchedule.getAllocationList().get(0).getDelayRange().getSize();
    }

    @Override
    public Iterator<? extends Move<Schedule>> createOriginalMoveIterator(ScoreDirector<Schedule> scoreDirector) {
        return new Iterator<Move<Schedule>>() {
            Allocation thisAllocation = scoreDirector.getWorkingSolution().getAllocationList().get(0);
            Iterator<Integer> delayIterator = thisAllocation.getDelayRange().createOriginalIterator();

            @Override
            public boolean hasNext() {
                return thisAllocation != null && delayIterator.hasNext();
            }

            @Override
            public Move<Schedule> next() {
                Allocation allocation = thisAllocation;
                Integer nextDelay = delayIterator.next();
                if (!delayIterator.hasNext()) {
                    thisAllocation = thisAllocation.getNextFocusedAllocation();
                    delayIterator = thisAllocation.getDelayRange().createOriginalIterator();
                }

                return new PreciseDelayMove(allocation, nextDelay);
            }
        };
    }

    @Override
    public Iterator<? extends Move<Schedule>> createRandomMoveIterator(ScoreDirector<Schedule> scoreDirector, Random random) {
        return new Iterator<Move<Schedule>>() {
            Allocation thisAllocation = scoreDirector.getWorkingSolution().getAllocationList().get(0);
            Iterator<Integer> delayIterator = thisAllocation.getDelayRange().createOriginalIterator();

            @Override
            public boolean hasNext() {
                return thisAllocation != null && delayIterator.hasNext();
            }

            @Override
            public Move<Schedule> next() {
                Allocation allocation = thisAllocation;
                Integer nextDelay = delayIterator.next();
                if (!delayIterator.hasNext()) {
                    thisAllocation = thisAllocation.getNextFocusedAllocation();
                    delayIterator = thisAllocation.getDelayRange().createOriginalIterator();
                }
                if (!delayIterator.hasNext()) {
                    do {
                        thisAllocation = thisAllocation.getNextFocusedAllocation();
                        delayIterator = thisAllocation.getDelayRange().createOriginalIterator();
                    } while (!DelayCanChange(thisAllocation));
                }
                return new PreciseDelayMove(allocation, nextDelay);
            }
        };
    }
}
