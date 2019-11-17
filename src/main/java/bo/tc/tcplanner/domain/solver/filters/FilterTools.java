package bo.tc.tcplanner.domain.solver.filters;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.AllocationType;

import java.util.List;

import static bo.tc.tcplanner.domain.DataStructureBuilder.dummyJob;

public class FilterTools {
    static boolean  isNotInIndex(Allocation allocation) {
//        return allocation.getIndex() <= allocation.getProject().getSchedule().getGlobalScheduleAfterIndex();
        List<Allocation> allocationList = allocation.getProject().getSchedule().getAllocationList();
//        return (allocationList.get(allocationList.size() - 2).getStartDate() - allocation.getStartDate()) > 60 * 24 * 3;
        return allocationList.get(allocationList.size() - 2).getIndex() - allocation.getIndex() > 500;
//        return false;
    }

    static boolean isLocked(Allocation allocation) {
        return allocation.getAllocationType() == AllocationType.Locked;
    }

    static boolean isDummy(Allocation allocation) {
        return allocation.getExecutionMode().getJob() == dummyJob;
    }

    public static boolean isNotMovable(Allocation allocation) {
        return !(allocation.getJob().getMovable() == 1);
    }

    static boolean isNotChangeable(Allocation allocation) {
        return !(allocation.getJob().getChangeable() == 1);
    }

    static boolean isNotSplittable(Allocation allocation) {
        return !(allocation.getJob().getSplittable() == 1);
    }
}
