package bo.tc.tcplanner.domain.solver;

import bo.tc.tcplanner.domain.Allocation;

import java.util.ArrayList;
import java.util.List;

public class ArrayListWithFilters extends ArrayList<Allocation> {

    public ArrayListWithFilters() {
        super();
    }

    public ArrayListWithFilters(List<Allocation> allocationList) {
        super();
        this.addAll(allocationList);
    }

    public ArrayListWithFilters addAllocation(Allocation allocation) {
        this.add(allocation);
        return this;
    }
}
