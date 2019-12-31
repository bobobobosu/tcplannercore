package bo.tc.tcplanner.domain.solver.filters;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.AllocationType;

import java.util.List;

import static bo.tc.tcplanner.datastructure.converters.DataStructureBuilder.dummyJob;

public class FilterTools {
    public static boolean isNotInIndex(Allocation allocation) {
//        return allocation.getIndex() <= allocation.getProject().getSchedule().getGlobalScheduleAfterIndex();
        List<Allocation> allocationList = allocation.getProject().getSchedule().getAllocationList();
//        return (allocationList.get(allocationList.size() - 2).getStartDate() - allocation.getStartDate()) > 60 * 24 * 3;
//        return allocationList.get(allocationList.size() - 2).getIndex() - allocation.getIndex() > 200;
        return false;
    }

    public static boolean isLocked(Allocation allocation) {
        return allocation.getAllocationTypeSet().contains(AllocationType.Locked) || !(allocation.getAllocationTypeSet().contains(AllocationType.Unlocked));
    }

    public static boolean isDummy(Allocation allocation) {
        return allocation.getExecutionMode().getJob() == dummyJob;
    }

    public static boolean isNotMovable(Allocation allocation) {
        return !(allocation.getExecutionMode().getChronoProperty().getMovable() == 1);
    }

    public static boolean isNotChangeable(Allocation allocation) {
        return !(allocation.getExecutionMode().getChronoProperty().getChangeable() == 1);
    }

    public static boolean isNotSplittable(Allocation allocation) {
        return !(allocation.getExecutionMode().getChronoProperty().getSplittable() == 1);
    }
}
