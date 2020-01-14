package bo.tc.tcplanner.domain.solver.comparators;

import bo.tc.tcplanner.domain.Allocation;

import java.util.Comparator;

public class AllocationDifficultyComparator implements Comparator<Allocation> {
    @Override
    public int compare(Allocation o1, Allocation o2) {
        return o2.getIndex().compareTo(o1.getIndex());
    }
}
