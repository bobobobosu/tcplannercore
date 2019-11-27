package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.datastructure.ResourceElement;
import bo.tc.tcplanner.domain.Allocation;

import java.util.HashMap;
import java.util.Map;

import static bo.tc.tcplanner.datastructure.converters.DataStructureBuilder.dummyLocation;

public class ListenerTools {
    public static void updateAllocationPreviousStandstill(Allocation allocation, Allocation prevAllocation) {
        if(prevAllocation == null){
            allocation.setPreviousStandstill(
                    allocation.getExecutionMode().getCurrentLocation());
        }else{
            if (!prevAllocation.getExecutionMode().getMovetoLocation()
                    .equals(dummyLocation)) {
                allocation.setPreviousStandstill(
                        prevAllocation.getExecutionMode().getMovetoLocation());
            } else {
                if (!prevAllocation.getExecutionMode().getCurrentLocation()
                        .equals(dummyLocation)) {
                    allocation.setPreviousStandstill(prevAllocation
                            .getExecutionMode().getCurrentLocation());
                } else {
                    allocation.setPreviousStandstill(
                            prevAllocation.getPreviousStandstill());
                }
            }
        }

    }

    public static void updateAllocationResourceStateChange(Allocation thisallocation, Allocation prevAllocation) {
        if (thisallocation.getResourceElementMap() == null) {
            thisallocation.setResourceElementMap(deepCloneResourceMap(thisallocation.getExecutionMode().getResourceStateChange().getResourceChange()));
        }
        if (prevAllocation != null) {
            thisallocation.setResourceElementMap(deepCloneResourceMap(prevAllocation.getResourceElementMap()));
        }


        for (Map.Entry<String, ResourceElement> resource : thisallocation.getExecutionMode().getResourceStateChange().getResourceChange().entrySet()) {
            if(!thisallocation.getResourceElementMap().containsKey(resource.getKey())){
                thisallocation.getResourceElementMap().put(resource.getKey(),new ResourceElement().setAmt(0));
            }
            double resourceAbsAmt = thisallocation.getResourceElementMap().get(resource.getKey()).getAmt();
            double resourceDeltaAmt = (resource.getValue().getAmt() * thisallocation.getProgressdelta()) / 100;
            double capacity = thisallocation.getProject().getSchedule().getValueEntryMap().get(resource.getKey()).getCapacity();
            double capped = resourceDeltaAmt + resourceAbsAmt <= capacity ? resourceDeltaAmt + resourceAbsAmt : resourceDeltaAmt + resourceAbsAmt - capacity;
            if(capped == 0){
                thisallocation.getResourceElementMap().remove(resource.getKey());
            }else{
                thisallocation.getResourceElementMap().get(resource.getKey()).setAmt(capped);
            }

        }

    }

    public static void CopyValueResourceMap(Map<String, ResourceElement> from_resourceElementMap, Map<String, ResourceElement> to_resourceElementMap) {
        for (Map.Entry<String, ResourceElement> resource : from_resourceElementMap.entrySet()) {
            to_resourceElementMap.get(resource.getKey()).setAmt(resource.getValue().getAmt());
        }
    }

    public static Map<String, ResourceElement> deepCloneResourceMap(Map<String, ResourceElement> resourceElementMap) {
        Map<String, ResourceElement> newMap = new HashMap<>();
        for (Map.Entry<String, ResourceElement> resource : resourceElementMap.entrySet()) {
            newMap.put(resource.getKey(), new ResourceElement(resource.getValue()));
        }
        return newMap;
    }
}
