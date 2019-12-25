package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveIteratorFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.Iterator;
import java.util.Random;

public class PreciseDelayMoveIteratorFactory implements MoveIteratorFactory<Schedule> {
    @Override
    public long getSize(ScoreDirector<Schedule> scoreDirector) {
        Schedule thisSchedule = scoreDirector.getWorkingSolution();
        return thisSchedule.getAllocationList().size() *
                thisSchedule.getAllocationList().get(0).getDelayRange().getSize();
    }

    @Override
    public Iterator<? extends Move<Schedule>> createOriginalMoveIterator(ScoreDirector<Schedule> scoreDirector) {
        return null;
    }

    @Override
    public Iterator<? extends Move<Schedule>> createRandomMoveIterator(ScoreDirector<Schedule> scoreDirector, Random random) {
        return null;
    }
}
