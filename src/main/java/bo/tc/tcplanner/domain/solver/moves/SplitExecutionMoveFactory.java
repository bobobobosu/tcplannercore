package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.ExecutionMode;
import bo.tc.tcplanner.domain.ExecutionModeType;
import bo.tc.tcplanner.domain.Schedule;
import bo.tc.tcplanner.domain.solver.listeners.DummyAllocationIterator;
import bo.tc.tcplanner.domain.solver.listeners.NonDummyAllocationIterator;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SplitExecutionMoveFactory implements MoveListFactory<Schedule> {
    @Override
    public List<SplitExecutionMove> createMoveList(Schedule schedule) {
        List<Allocation> dummyAllocationList = new ArrayList<>();
        Allocation thisAllocation = schedule.getAllocationList().get(0);
        while ((thisAllocation = NonDummyAllocationIterator.getNext(thisAllocation)) != null) {
            Allocation dummyAllocation;
            if ((dummyAllocation = DummyAllocationIterator.getNext(thisAllocation)) != null) {
                dummyAllocationList.add(dummyAllocation);
            }
        }

        List<SplitExecutionMove> moveList = new ArrayList<>();
        thisAllocation = schedule.getAllocationList().get(0);
        while ((thisAllocation = NonDummyAllocationIterator.getNext(thisAllocation)) != null) {
            for (Allocation dummyAllocation : dummyAllocationList) {
                for (ExecutionMode usableExecutionMode : thisAllocation.getJob().getExecutionModeList()
                        .stream()
                        .filter(x -> x.getExecutionModeTypes().contains(ExecutionModeType.USABLE))
                        .collect(Collectors.toList())) {
                    moveList.add(new SplitExecutionMove(thisAllocation, dummyAllocation, usableExecutionMode));
                }
            }
        }
        return moveList;
    }
}
