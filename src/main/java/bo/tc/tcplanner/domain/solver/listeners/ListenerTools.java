package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.datastructure.LocationHierarchyMap;
import bo.tc.tcplanner.datastructure.ResourceElement;
import bo.tc.tcplanner.domain.Allocation;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static bo.tc.tcplanner.app.TCSchedulingApp.locationHierarchyMap;
import static bo.tc.tcplanner.datastructure.converters.DataStructureBuilder.dummyLocation;

public class ListenerTools {
    public static void updatePlanningDuration(Allocation allocation) {
        allocation.setPlannedDuration(allocation.getExecutionMode().getTimeduration().multipliedBy(allocation.getProgressdelta()).dividedBy(100));
    }

    public static void updatePredecessorsDoneDate(Allocation allocation, Allocation prevAllocation) {
        try {
            allocation.setPredecessorsDoneDate(prevAllocation == null ? allocation.getProject().getSchedule().getGlobalStartTime() : prevAllocation.getEndDate());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void updateAllocationPreviousStandstill(Allocation allocation, Allocation prevAllocation) {
        String PreviousStandStill = prevAllocation.getPreviousStandstill();

        if (!locationHierarchyMap.containsKey(PreviousStandStill) ||
                !locationHierarchyMap.get(PreviousStandStill).contains(
                        prevAllocation.getExecutionMode().getCurrentLocation())) {
            PreviousStandStill = prevAllocation.getExecutionMode().getCurrentLocation();
        }

        if (!locationHierarchyMap.containsKey(PreviousStandStill) ||
                !locationHierarchyMap.get(PreviousStandStill).contains(
                        prevAllocation.getExecutionMode().getMovetoLocation())) {
            PreviousStandStill = prevAllocation.getExecutionMode().getMovetoLocation();
        }

        allocation.setPreviousStandstill(PreviousStandStill);
    }

    public static void updateAllocationResourceStateChange(Allocation thisallocation, Allocation prevAllocation) {
        thisallocation.setResourceElementMap(deepCloneResourceMap(prevAllocation.getResourceElementMap()));

        for (Map.Entry<String, List<ResourceElement>> resource : thisallocation.getExecutionMode().getResourceStateChange().getResourceChange().entrySet()) {
            if (!thisallocation.getResourceElementMap().containsKey(resource.getKey())) {
                thisallocation.getResourceElementMap().put(resource.getKey(), new ArrayList<>());
            }
            double resourceAbsAmt = thisallocation.getResourceElementMap().get(resource.getKey())
                    .stream()
                    .mapToDouble(ResourceElement::getAmt)
                    .sum();
            double resourceDeltaAmt = resource.getValue()
                    .stream()
                    .mapToDouble(ResourceElement::getAmt)
                    .sum() * (thisallocation.getProgressdelta().doubleValue() / (100 * thisallocation.getExecutionMode().getProgressChange().getProgressDelta()));
            double appliedAmt = resourceAbsAmt + resourceDeltaAmt;
            double capacity = thisallocation.getProject().getSchedule().getValueEntryMap().get(resource.getKey()).getCapacity();
            double capped = appliedAmt > capacity ? resourceAbsAmt : appliedAmt;
            thisallocation.getResourceElementMap().put(resource.getKey(),
                    capacity != 0 ?
                            Arrays.asList(new ResourceElement().setAmt(capped)) :
                            new ArrayList<>());

        }

    }



    public static void resourcePullFromList(
            ResourceElement resourceElement,
            List<ResourceElement> resourceElementList) {



    }

    public static boolean isPullable(ResourceElement resourceReq, ResourceElement resourceElementPro) {
        return true;
    }


    private static Map<String, List<ResourceElement>> deepCloneResourceMap(Map<String, List<ResourceElement>> resourceElementMap) {
        Map<String, List<ResourceElement>> newMap = new HashMap<>();
        for (Map.Entry<String, List<ResourceElement>> resource : resourceElementMap.entrySet()) {
            newMap.put(resource.getKey(), resource.getValue()
                    .stream().map(x -> new ResourceElement()
                            .setAmt(x.getAmt())
                            .setLocation(x.getLocation())
                            .setVolatileFlag(x.isVolatileFlag()))
                    .collect(Collectors.toList())
            );
        }
        return newMap;
    }
}
