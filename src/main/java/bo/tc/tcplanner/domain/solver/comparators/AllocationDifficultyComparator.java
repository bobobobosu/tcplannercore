//package bo.tc.tcplanner.domain.solver.comparators;
//
//import bo.tc.tcplanner.domain.Allocation;
//
//import java.util.Comparator;
//
//import static bo.tc.tcplanner.datastructure.converters.DataStructureBuilder.dummyJob;
//
//public class AllocationDifficultyComparator implements Comparator<Allocation> {
//    @Override
//    public int compare(Allocation o1, Allocation o2) {
//        return o2.getIndex().compareTo(o1.getIndex());
////        return dummyRank(o2).compareTo(dummyRank(o1));
//    }
//
//    Integer dummyRank(Allocation allocation) {
//        int rank = 0;
//        if (allocation.isFocused()) rank += 1;
//        Allocation search;
//
//        search = allocation;
//        while (search.getSuccessorAllocationList().size() > 0) {
//            search = allocation.getSuccessorAllocationList().get(0);
//            if (search.isFocused()) rank += 1;
//        }
//
//        search = allocation;
//        while (search.getPredecessorAllocationList().size() > 0) {
//            search = allocation.getSuccessorAllocationList().get(0);
//            if (search.isFocused()) rank += 1;
//        }
//
//        return rank;
//    }
//}
