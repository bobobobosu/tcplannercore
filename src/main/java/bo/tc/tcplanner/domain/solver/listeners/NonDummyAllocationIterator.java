package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.datastructure.converters.DataStructureBuilder;
import bo.tc.tcplanner.domain.Allocation;
import org.kie.api.definition.rule.All;

import java.util.*;

public class NonDummyAllocationIterator {
    public static Allocation getNext(Allocation allocation) {
        Allocation pointerAllocation = allocation;
        Allocation nextAllocation = null;
        while (true) {
            if (pointerAllocation.getSuccessorAllocationList().size() > 0) {
                Allocation candidate = pointerAllocation.getSuccessorAllocationList().get(0);
                if (candidate.getJob() != DataStructureBuilder.dummyJob) {
                    nextAllocation = candidate;
                    break;
                } else {
                    pointerAllocation = candidate;
                }
            } else {
                break;
            }
        }
        return nextAllocation;
    }


    static List<Allocation> getAllNext(Allocation allocation) {
        Allocation thisAllocation = allocation;
        List<Allocation> allocationList = new LinkedList<>();
        while ((thisAllocation = getNext(thisAllocation)) != null) {
            allocationList.add(thisAllocation);
        }
        return allocationList;
    }

    public static List<Allocation> getAllNextIncludeThis(Allocation allocation) {
        List<Allocation> allocationList = getAllNext(allocation);
        allocationList.add(0, allocation);
        return allocationList;
    }

    public static List<Allocation> getAllFocused(Allocation allocation){
        return getAllNextIncludeThis(allocation.getSourceAllocation());
    }

    static Allocation getPrev(Allocation allocation) {
        Allocation pointerAllocation = allocation;
        Allocation prevAllocation = null;
        while (true) {
            if (pointerAllocation.getPredecessorAllocationList().size() > 0) {
                Allocation candidate = pointerAllocation.getPredecessorAllocationList().get(0);
                if (candidate.getJob() != DataStructureBuilder.dummyJob) {
                    prevAllocation = candidate;
                    break;
                } else {
                    pointerAllocation = candidate;
                }
            } else {
                break;
            }
        }
        return prevAllocation;
    }
}
