package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PreciseDelayMoveFactory implements MoveListFactory<Schedule> {

    @Override
    public List<PreciseDelayMove> createMoveList(Schedule schedule) {
        List<PreciseDelayMove> moveList = new ArrayList<>();
        for (Allocation allocation : schedule.getAllocationList()) {
            Iterator<Integer> delayIterator = allocation.getDelayRange().createOriginalIterator();
            while (delayIterator.hasNext()) {
                moveList.add(new PreciseDelayMove(allocation, delayIterator.next()));
            }
        }

//        Collections.reverse(moveList);
        return moveList;
    }
}
