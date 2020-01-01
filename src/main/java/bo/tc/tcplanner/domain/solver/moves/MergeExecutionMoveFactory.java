package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.ExecutionMode;
import bo.tc.tcplanner.domain.Schedule;

import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;

import java.util.*;
import java.util.stream.Collectors;

public class MergeExecutionMoveFactory implements MoveListFactory<Schedule> {
    @Override
    public List<MergeExecutionMove> createMoveList(Schedule schedule) {
        Map<ExecutionMode, List<Allocation>> map = schedule
                .getAllocationList()
                .stream()
                .collect(Collectors.groupingBy(Allocation::getExecutionMode));

        List<MergeExecutionMove> moveList = new ArrayList<>();
        for (Allocation thisAllocation : schedule.getFocusedAllocationList()) {
            if (map.containsKey(thisAllocation.getExecutionMode())) {
                for (Allocation toAllocation : map.get(thisAllocation.getExecutionMode())) {
                    moveList.add(new MergeExecutionMove(thisAllocation, toAllocation));
                }
            }
        }

        return moveList;
    }
}

