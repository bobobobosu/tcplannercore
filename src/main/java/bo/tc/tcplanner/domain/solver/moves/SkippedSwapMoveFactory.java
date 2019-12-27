package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.ExecutionMode;
import bo.tc.tcplanner.domain.Schedule;
import bo.tc.tcplanner.domain.solver.listeners.DummyAllocationIterator;
import bo.tc.tcplanner.domain.solver.listeners.NonDummyAllocationIterator;
import org.kie.api.definition.rule.All;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;
import org.optaplanner.core.impl.heuristic.selector.move.generic.SwapMove;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SkippedSwapMoveFactory implements MoveListFactory<Schedule> {
    @Override
    public List<SkippedSwapMove> createMoveList(Schedule schedule) {
        List<Allocation> dummyAllocationList = new ArrayList<>();
        Allocation thisAllocation = schedule.getAllocationList().get(0);
        while ((thisAllocation = NonDummyAllocationIterator.getNext(thisAllocation)) != null) {
            Allocation dummyAllocation;
            if ((dummyAllocation = DummyAllocationIterator.getNext(thisAllocation)) != null) {
                dummyAllocationList.add(dummyAllocation);
            }
        }

        List<SkippedSwapMove> moveList = new ArrayList<>();
        thisAllocation = schedule.getAllocationList().get(0);
        while ((thisAllocation = NonDummyAllocationIterator.getNext(thisAllocation)) != null) {
            for (Allocation dummyAllocation : dummyAllocationList) {
                moveList.add(new SkippedSwapMove(thisAllocation, dummyAllocation));
            }
        }
        return moveList;
    }
}
