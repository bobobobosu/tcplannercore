package bo.tc.tcplanner.domain.solver.filters;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.AllocationType;

import java.util.List;

public class FilterTools {
    public static boolean DelayCanChange(Allocation allocation) {
        if (allocation.getAllocationTypeSet().contains(AllocationType.Locked)) return false;
        if (allocation.getExecutionMode().getChronoProperty().getMovable() != 1) return false;
        return true;
    }

    public static boolean ExecutionModeCanChange(Allocation allocation) {
        if (allocation.getAllocationTypeSet().contains(AllocationType.Locked)) return false;
        if (allocation.getExecutionMode().getChronoProperty().getChangeable() != 1) return false;
        return true;
    }

    public static boolean ProgressDeltaCanChange(Allocation allocation) {
        if (allocation.getAllocationTypeSet().contains(AllocationType.Locked)) return false;
        if (allocation.getExecutionMode().getChronoProperty().getSplittable() != 1) return false;
        return true;
    }

    public static boolean IsFocused(Allocation allocation) {
        if (!allocation.isFocused()) return false;
        return true;
    }
}
