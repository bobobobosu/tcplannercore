package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveIteratorFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.Iterator;
import java.util.Random;

public class SkippedSwapMoveIteratorFactory implements MoveIteratorFactory<Schedule> {
    @Override
    public long getSize(ScoreDirector<Schedule> scoreDirector) {
        return scoreDirector.getWorkingSolution().focusedAllocationSet.size() *
                scoreDirector.getWorkingSolution().focusedAllocationSet.size();
    }

    @Override
    public Iterator<? extends Move<Schedule>> createOriginalMoveIterator(ScoreDirector<Schedule> scoreDirector) {
        return new Iterator<Move<Schedule>>() {
            Iterator<Allocation> focusedAllocationIterator = scoreDirector.getWorkingSolution().focusedAllocationSet.iterator();
            Iterator<Allocation> dummyAllocationIterator = scoreDirector.getWorkingSolution().getDummyAllocationIterator();
            Allocation thisAllocation = focusedAllocationIterator.next();

            @Override
            public boolean hasNext() {
                return thisAllocation != null;
            }

            @Override
            public Move<Schedule> next() {
                Allocation allocation = thisAllocation;
                if (!dummyAllocationIterator.hasNext()) {
                    thisAllocation = focusedAllocationIterator.hasNext() ? focusedAllocationIterator.next() : null;
                    dummyAllocationIterator = scoreDirector.getWorkingSolution().getDummyAllocationIterator();
                }
                return new SkippedSwapMove(allocation, dummyAllocationIterator.next());
            }
        };
    }

    @Override
    public Iterator<? extends Move<Schedule>> createRandomMoveIterator(ScoreDirector<Schedule> scoreDirector, Random random) {
        return new Iterator<Move<Schedule>>() {
            Iterator<Allocation> focusedAllocationIterator = scoreDirector.getWorkingSolution().focusedAllocationSet.iterator();
            Iterator<Allocation> dummyAllocationIterator = scoreDirector.getWorkingSolution().getDummyAllocationIterator();
            Move<Schedule> thisMove = new SkippedSwapMove(focusedAllocationIterator.next(), dummyAllocationIterator.next());

            @Override
            public boolean hasNext() {
                return thisMove != null;
            }

            @Override
            public Move<Schedule> next() {
                Move<Schedule> saveMove = thisMove;
                if (!dummyAllocationIterator.hasNext())
                    dummyAllocationIterator = scoreDirector.getWorkingSolution().getDummyAllocationIterator();
                thisMove = new SkippedSwapMove(focusedAllocationIterator.next(), dummyAllocationIterator.next());
                return saveMove;
            }
        };
    }
}
