package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.datastructure.LocationHierarchyMap;
import bo.tc.tcplanner.datastructure.ResourceElement;
import bo.tc.tcplanner.domain.Allocation;

import java.util.HashMap;
import java.util.Map;

import static bo.tc.tcplanner.app.TCSchedulingApp.locationHierarchyMap;
import static bo.tc.tcplanner.datastructure.converters.DataStructureBuilder.dummyLocation;

public class ListenerTools {
    public static void updatePlanningDuration(Allocation allocation) {
        allocation.setPlannedDuration(allocation.getExecutionMode().getTimeduration() * allocation.getProgressdelta() / 100);
    }

    public static void updatePredecessorsDoneDate(Allocation allocation, Allocation prevAllocation) {
        try {
            allocation.setPredecessorsDoneDate(prevAllocation == null ? 0 : prevAllocation.getEndDate());
        } catch (Exception ex) {
            ex.printStackTrace();
            int i = 0;
        }

    }

    public static void updateAllocationPreviousStandstill(Allocation allocation, Allocation prevAllocation) {
        String PreviousStandStill = prevAllocation.getPreviousStandstill();

        if(!locationHierarchyMap.containsKey(PreviousStandStill) ||
                !locationHierarchyMap.get(PreviousStandStill).contains(
                        prevAllocation.getExecutionMode().getCurrentLocation())){
            PreviousStandStill = prevAllocation.getExecutionMode().getCurrentLocation();
        }

        if(!locationHierarchyMap.containsKey(PreviousStandStill) ||
                !locationHierarchyMap.get(PreviousStandStill).contains(
                        prevAllocation.getExecutionMode().getMovetoLocation())){
            PreviousStandStill = prevAllocation.getExecutionMode().getMovetoLocation();
        }

        allocation.setPreviousStandstill(PreviousStandStill);
    }

    public static void updateAllocationResourceStateChange(Allocation thisallocation, Allocation prevAllocation) {
        thisallocation.setResourceElementMap(deepCloneResourceMap(prevAllocation.getResourceElementMap()));

        for (Map.Entry<String, ResourceElement> resource : thisallocation.getExecutionMode().getResourceStateChange().getResourceChange().entrySet()) {
            if (!thisallocation.getResourceElementMap().containsKey(resource.getKey())) {
                thisallocation.getResourceElementMap().put(resource.getKey(), new ResourceElement().setAmt(0));
            }
            double resourceAbsAmt = thisallocation.getResourceElementMap().get(resource.getKey()).getAmt();
            double resourceDeltaAmt = resource.getValue().getAmt() * (thisallocation.getProgressdelta() / (100 * thisallocation.getExecutionMode().getProgressChange().getProgressDelta()));
            double capacity = thisallocation.getProject().getSchedule().getValueEntryMap().get(resource.getKey()).getCapacity();
            double capped = resourceDeltaAmt + resourceAbsAmt <= capacity ? resourceDeltaAmt + resourceAbsAmt : capacity - (resourceDeltaAmt + resourceAbsAmt);
            if (capped == 0) {
                thisallocation.getResourceElementMap().remove(resource.getKey());
            } else {
                thisallocation.getResourceElementMap().get(resource.getKey()).setAmt(capped);
            }

        }

    }


    private static Map<String, ResourceElement> deepCloneResourceMap(Map<String, ResourceElement> resourceElementMap) {
        Map<String, ResourceElement> newMap = new HashMap<>();
        for (Map.Entry<String, ResourceElement> resource : resourceElementMap.entrySet()) {
            newMap.put(resource.getKey(), new ResourceElement(resource.getValue()));
        }
        return newMap;
    }
}
