package bo.tc.tcplanner.domain.solver.filters;

import bo.tc.tcplanner.domain.Allocation;

public class FilterTools {
    public static boolean DelayCanChange(Allocation allocation) {
        if (allocation.isHistory()) return false;
        if (allocation.getTimelineEntry().getChronoProperty().getMovable() != 1) return false;
        return true;
    }

    public static boolean TimelineEntryCanChange(Allocation allocation) {
        if (allocation.isHistory()) return false;
        if (allocation.getTimelineEntry().getChronoProperty().getChangeable() != 1) return false;
        return true;
    }

    public static boolean ProgressDeltaCanChange(Allocation allocation) {
        if (allocation.isHistory()) return false;
        if (allocation.getTimelineEntry().getChronoProperty().getSplittable() != 1) return false;
        return true;
    }

    public static boolean IsFocused(Allocation allocation) {
        if (!allocation.isFocused()) return false;
        return true;
    }
}
