package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.AllocationType;
import bo.tc.tcplanner.domain.ExecutionMode;
import bo.tc.tcplanner.domain.Schedule;
import bo.tc.tcplanner.domain.solver.listeners.DummyAllocationIterator;
import bo.tc.tcplanner.domain.solver.listeners.NonDummyAllocationIterator;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;

import java.util.*;

import static bo.tc.tcplanner.domain.solver.filters.FilterTools.*;

public class PreciseExecutionMoveFactory implements MoveListFactory<Schedule> {

    @Override
    public List<SetValueMove> createMoveList(Schedule schedule) {
        List<Allocation> allocationList = new ArrayList<>();
//        Allocation thisAllocation = schedule.getAllocationList().get(0);
//        while ((thisAllocation = NonDummyAllocationIterator.getNext(thisAllocation)) != null) {
//            allocationList.add(thisAllocation);
//            Allocation dummyAllocation;
//            if ((dummyAllocation = DummyAllocationIterator.getNext(thisAllocation)) != null) {
//                allocationList.add(dummyAllocation);
//            }
//        }

        Allocation thisAllocation = schedule.getAllocationList().get(0);
        Allocation prevAllocation;
        while ((thisAllocation = NonDummyAllocationIterator.getNext(prevAllocation = thisAllocation)) != null) {
            allocationList.add(schedule.getAllocationList().get((
                    thisAllocation.getIndex() + prevAllocation.getIndex()
            ) / 2));
        }

        List<SetValueMove> moveList = new ArrayList<>();
        for (Allocation allocation : allocationList) {
            if (isLocked(allocation)) continue;
            if (isNotChangeable(allocation)) continue;
            if (isNotInIndex(allocation)) continue;
            for (ExecutionMode executionMode : allocation.getExecutionModeRange()) {
                moveList.add(new SetValueMove(
                        Arrays.asList(allocation),
                        Arrays.asList(new AllocationValues()
                                .setExecutionMode(executionMode)
                                .setProgressDelta(10))
                ));
            }
        }
        Collections.reverse(moveList);
        return moveList;

    }
}
