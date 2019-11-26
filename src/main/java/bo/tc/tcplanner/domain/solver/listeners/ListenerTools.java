package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.domain.Allocation;

import static bo.tc.tcplanner.datastructure.converters.DataStructureBuilder.dummyLocation;

public class ListenerTools {
    public static void updateAllocationPreviousStandstill(Allocation allocation) {

        Allocation prevAlloction = allocation.getPredecessorAllocationList().get(0);
        if (!prevAlloction.getExecutionMode().getMovetoLocation()
                .equals(dummyLocation)) {
            allocation.setPreviousStandstill(
                    prevAlloction.getExecutionMode().getMovetoLocation());
        } else {
            if (!prevAlloction.getExecutionMode().getCurrentLocation()
                    .equals(dummyLocation)) {
                allocation.setPreviousStandstill(prevAlloction
                        .getExecutionMode().getCurrentLocation());
            } else {
                allocation.setPreviousStandstill(
                        prevAlloction.getPreviousStandstill());
            }
        }
    }

//    public static void updateAllocationResourceStateChange(Allocation allocation) {
//        if(allocation.getPredecessorAllocationList().size()==0)return;
//        Allocation prevAllocation = allocation.getPredecessorAllocationList().get(0);
//        HashMap<String, ResourceElement> prevResourceChange_abs = prevAllocation.getResourceStateChange_absolute().getResourceChange();
//        HashMap<String, ResourceElement> thisResourceChange_delta = allocation.getExecutionMode().getResourceStateChange().getResourceChange();
//        HashMap<String, ResourceElement> thisResourceChange_abs = allocation.getResourceStateChange_absolute().getResourceChange();
//
//        //Copy
//        for (Map.Entry<String, ResourceElement> resource : prevResourceChange_abs.entrySet()) {
//            thisResourceChange_abs.get(resource.getKey()).setAmt(resource.getValue().getAmt());
//            thisResourceChange_abs.get(resource.getKey()).setProductionLocation(resource.getValue().getProductionLocation());
//            thisResourceChange_abs.get(resource.getKey()).setRequirementLocation(resource.getValue().getRequirementLocation());
//        }
//
//        //Apply
//        for (Map.Entry<String, ResourceElement> resource : thisResourceChange_delta.entrySet()) {
//            double resourceAbsAmt = thisResourceChange_abs.get(resource.getKey()).getAmt();
//            double resourceDeltaAmt = (resource.getValue().getAmt() * allocation.getProgressdelta()) / 100;
//            thisResourceChange_abs.get(resource.getKey()).setAmt(resourceAbsAmt+resourceDeltaAmt);
//        }
//
//
//
//    }

}
