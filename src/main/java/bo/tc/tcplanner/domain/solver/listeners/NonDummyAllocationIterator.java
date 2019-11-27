package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.datastructure.converters.DataStructureBuilder;
import bo.tc.tcplanner.domain.Allocation;

import java.util.Iterator;

public class NonDummyAllocationIterator implements Iterator<Allocation> {
    private Allocation firstAllocation;
    private Allocation thisAllocation;

    public NonDummyAllocationIterator(Allocation firstAllocation) {
        this.firstAllocation = firstAllocation;
        this.thisAllocation = null;
    }

    @Override
    public boolean hasNext() {
        return getNext(thisAllocation) != null;
    }

    @Override
    public Allocation next() {
        Allocation nextAllocation = getNext(thisAllocation);
        this.thisAllocation = nextAllocation;
        return nextAllocation;
    }

    private Allocation getNext(Allocation allocation){
        if(thisAllocation == null) return firstAllocation;
        Allocation nextAllocation = null;
        while(true){
            if(allocation.getSuccessorAllocationList().size() > 0){
                Allocation candidate = allocation.getSuccessorAllocationList().get(0);
                if(candidate.getJob() != DataStructureBuilder.dummyJob){
                    nextAllocation = candidate;
                    break;
                }else{
                    allocation = candidate;
                }
            }else{
                break;
            }
        }
        return nextAllocation;
    }

    public static Allocation getPrev(Allocation allocation){
        Allocation pointerAllocation = allocation;
        Allocation prevAllocation = null;
        while(true){
            if(pointerAllocation.getPredecessorAllocationList().size() > 0){
                Allocation candidate = pointerAllocation.getPredecessorAllocationList().get(0);
                if(candidate.getJob() != DataStructureBuilder.dummyJob){
                    prevAllocation = candidate;
                    break;
                }else{
                    pointerAllocation = candidate;
                }
            }else{
                break;
            }
        }
        return prevAllocation;
    }
}
