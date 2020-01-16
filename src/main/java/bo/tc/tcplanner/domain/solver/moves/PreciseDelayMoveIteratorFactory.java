package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveIteratorFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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
            Iterator<Allocation> focusedAllocationIterator = scoreDirector.getWorkingSolution().focusedAllocationSet.iterator();
            Allocation thisAllocation = focusedAllocationIterator.next();
            Iterator<Integer> delayIterator = thisAllocation.getDelayRange().createOriginalIterator();

            @Override
            public boolean hasNext() {
                return thisAllocation != null && delayIterator.hasNext();
            }

            @Override
            public Move<Schedule> next() {
                Allocation allocation = thisAllocation;
                if (!delayIterator.hasNext()) {
                    thisAllocation = focusedAllocationIterator.hasNext() ? focusedAllocationIterator.next() : null;
                    if (thisAllocation != null)
                        delayIterator = thisAllocation.getDelayRange().createOriginalIterator();
                }

                return new PreciseDelayMove(allocation, delayIterator.next());
            }
        };
    }

    @Override
    public Iterator<? extends Move<Schedule>> createRandomMoveIterator(ScoreDirector<Schedule> scoreDirector, Random random) {
        return new Iterator<Move<Schedule>>() {
            List<Allocation> focusedAllocationList = new ArrayList<>(scoreDirector.getWorkingSolution().focusedAllocationSet);

            @Override
            public boolean hasNext() {
                return focusedAllocationList.size() > 0;
            }

            @Override
            public Move<Schedule> next() {
                Allocation thisAllocation = focusedAllocationList.get(random.nextInt(focusedAllocationList.size()));
                return new PreciseDelayMove(thisAllocation, thisAllocation.getDelayRange().get(random.nextInt((int) thisAllocation.getDelayRange().getSize())));
            }
        };
    }
}
