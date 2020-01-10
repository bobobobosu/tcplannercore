package bo.tc.tcplanner.domain.solver.filters;

import bo.tc.tcplanner.domain.Allocation;

public class FilterTools {
    public static boolean DelayCanChange(Allocation allocation) {
        if (allocation.isPinned()) return false;
        if (allocation.isHistory()) return false;
        return allocation.getTimelineEntry().getChronoProperty().getMovable() == 1;
    }

    public static boolean TimelineEntryCanChange(Allocation allocation) {
        if (allocation.isPinned()) return false;
        if (allocation.isHistory()) return false;
        return allocation.getTimelineEntry().getChronoProperty().getChangeable() == 1;
    }

    public static boolean ProgressDeltaCanChange(Allocation allocation) {
        if (allocation.isPinned()) return false;
        if (allocation.isHistory()) return false;
        return allocation.getTimelineEntry().getChronoProperty().getSplittable() == 1;
    }

    public static boolean IsFocused(Allocation allocation) {
        return allocation.isFocused();
    }
}
