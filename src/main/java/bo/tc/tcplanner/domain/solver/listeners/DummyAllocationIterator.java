package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.datastructure.converters.DataStructureBuilder;
import bo.tc.tcplanner.domain.Allocation;

import java.util.ArrayList;
import java.util.List;

import static bo.tc.tcplanner.datastructure.converters.DataStructureBuilder.dummyJob;

public class DummyAllocationIterator {
    public static Allocation getNext(Allocation allocation) {
        if (allocation.getSuccessorAllocationList().size() == 0 ||
                allocation.getSuccessorAllocationList().get(0).getJob() != dummyJob) return null;
        Allocation untilAllocation;
        Allocation thisAllocation = allocation;
        List<Allocation> dummyAllocations = new ArrayList<>();
        if ((untilAllocation = NonDummyAllocationIterator.getNext(allocation)) == null)
            untilAllocation = allocation.getSourceAllocation();
        while ((thisAllocation = thisAllocation.getSuccessorAllocationList().get(0)) != untilAllocation)
            dummyAllocations.add(thisAllocation);
        return dummyAllocations.get(dummyAllocations.size() / 2);
    }
}
