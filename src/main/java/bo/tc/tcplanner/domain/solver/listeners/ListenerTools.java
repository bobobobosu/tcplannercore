package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.datastructure.LocationHierarchyMap;
import bo.tc.tcplanner.datastructure.ResourceElement;
import bo.tc.tcplanner.domain.Allocation;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static bo.tc.tcplanner.app.DroolsTools.locationRestrictionCheck;
import static bo.tc.tcplanner.app.TCSchedulingApp.locationHierarchyMap;
import static bo.tc.tcplanner.app.TCSchedulingApp.valueEntryMap;
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

//    public static void updateAllocationResourceStateChange(Allocation thisallocation, Allocation prevAllocation) {
//        thisallocation.setResourceElementMap(deepCloneResourceMap(prevAllocation.getResourceElementMap()));
//
//        for (Map.Entry<String, List<ResourceElement>> resource : thisallocation.getExecutionMode().getResourceStateChange().getResourceChange().entrySet()) {
//            if (!thisallocation.getResourceElementMap().containsKey(resource.getKey())) {
//                thisallocation.getResourceElementMap().put(resource.getKey(), new ArrayList<>());
//            }
//            double resourceAbsAmt = thisallocation.getResourceElementMap().get(resource.getKey())
//                    .stream()
//                    .mapToDouble(ResourceElement::getAmt)
//                    .sum();
//            double resourceDeltaAmt = resource.getValue()
//                    .stream()
//                    .mapToDouble(ResourceElement::getAmt)
//                    .sum() * (thisallocation.getProgressdelta().doubleValue() / (100 * thisallocation.getExecutionMode().getProgressChange().getProgressDelta()));
//            double appliedAmt = resourceAbsAmt + resourceDeltaAmt;
//            double capacity = thisallocation.getProject().getSchedule().getValueEntryMap().get(resource.getKey()).getCapacity();
//            double capped = appliedAmt > capacity ? resourceAbsAmt : appliedAmt;
//            thisallocation.getResourceElementMap().put(resource.getKey(),
//                    capacity != 0 ?
//                            Arrays.asList(new ResourceElement().setAmt(capped)) :
//                            new ArrayList<>());
//
//        }
//
//    }

    public static void updateAllocationResourceStateChange(List<Allocation> focusedAllocationList) {
        // Reset Resource Element maps
        for (Allocation thisAllocation : focusedAllocationList) {
            thisAllocation.setResourceElementMap(
                    deepCloneResourceMap(thisAllocation.getExecutionMode().getResourceStateChange().getResourceChange())
            );
            List<Integer> priorityTimelineIdList = thisAllocation.getJob().getTimelineProperty().getDependencyIdList();
            thisAllocation.getResourceElementMap().forEach((k, v) ->
                    v.forEach(x -> x.setPriorityTimelineIdList(priorityTimelineIdList)));
        }

        // Pull

        for (int i = 0; i < focusedAllocationList.size(); i++) {
            Allocation thisAllocation = focusedAllocationList.get(i);

            // Build pull Queue
            List<Allocation> nextAllocations = focusedAllocationList.subList(i, focusedAllocationList.size());

            Map<String, List<ResourceElement>> pullList = thisAllocation.getExecutionMode().getResourceStateChange()
                    .getResourceChange()
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            x -> (nextAllocations
                                    .stream()
                                    .filter(y -> y.getResourceElementMap().containsKey(x.getKey()))
                                    .flatMap(y -> y.getResourceElementMap().get(x.getKey()).stream())
                                    .filter(y -> y.getAmt() < 0)
                                    .collect(Collectors.toList()))
                    ));

            Integer thisTimelineId = thisAllocation.getJob().getTimelineProperty().getTimelineid();
            pullList.forEach((k, v) -> v.sort(
                    (o1, o2) -> {
                        return new CompareToBuilder()
                                .append(o1.getPriorityTimelineIdList().contains(thisTimelineId),
                                        o2.getPriorityTimelineIdList().contains(thisTimelineId))
                                .append(o1.getPriorityTimelineIdList().size(), o2.getPriorityTimelineIdList().size())
                                .append(locationRestrictionCheck(o2.getLocation(), o1.getLocation()),
                                        locationRestrictionCheck(o1.getLocation(), o2.getLocation()))
                                .append(o1.getAmt(), o2.getAmt())
                                .toComparison();
                    }
            ));

            // Pull
            doPull(thisAllocation.getResourceElementMap(), pullList);
        }

        // Clean
        focusedAllocationList.forEach(
                x -> {
                    x.getResourceElementMap()
                            .forEach((key, value) -> value.removeIf(z -> z.getAmt() == 0));
                    x.getResourceElementMap().entrySet().removeIf(y -> y.getValue().size() == 0);
                }
        );

        // Consolidate
        focusedAllocationList.forEach(x -> x.getResourceElementMap().forEach((key1, value1) -> {
            Map<String, List<ResourceElement>> groupByLocation =
                    value1.stream()
                            .filter(y -> y.getAmt() > 0)
                            .collect(Collectors.groupingBy(ResourceElement::getLocation));
            value1.removeIf(t -> t.getAmt() > 0);
            groupByLocation.forEach((key, value) -> value1.add(new ResourceElement()
                    .setLocation(key)
                    .setVolatileFlag(value.get(0).isVolatileFlag())
                    .setAmt(
                            Math.min(
                                    valueEntryMap.containsKey(key1) ? valueEntryMap.get(key1).getCapacity() : Integer.MAX_VALUE
                                    , value.stream().mapToDouble(ResourceElement::getAmt).sum())
                    )));
        }));

        // Create Absolute
        for (int i = 1; i < focusedAllocationList.size(); i++) {
            Map<String, List<ResourceElement>> prevR = focusedAllocationList.get(i - 1).getResourceElementMap();
            Map<String, List<ResourceElement>> thisR = focusedAllocationList.get(i).getResourceElementMap();
            prevR.forEach((k, v) -> {
                if (!thisR.containsKey(k)) thisR.put(k, new ArrayList<>());
                thisR.get(k).addAll(v.stream().filter(x->x.getAmt()>0).collect(Collectors.toList()));
            });
        }


    }

    private static void doPull(Map<String, List<ResourceElement>> resourceElementMap, Map<String, List<ResourceElement>> pullList) {
        resourceElementMap.forEach((k, v) -> v.forEach(
                x -> {
                    if (x.getAmt() > 0) {
                        boolean changing = true;
                        while (changing && pullList.get(k).size() > 0) {
                            assert pullList.get(k).stream().allMatch(o -> o.getAmt() < 0);
                            changing = false;
                            ResourceElement nextResourceElement = pullList.get(k).get(0);
                            assert nextResourceElement.getAmt() < 0;
                            double delta;
                            if ((locationRestrictionCheck(x.getLocation(), "") || true) &&
                                    (delta = Math.min(x.getAmt(), -nextResourceElement.getAmt())) > 0) {
                                x.setAmt(x.getAmt() - delta);
                                nextResourceElement.setAmt(nextResourceElement.getAmt() + delta);
                                changing = true;
                            }

                            if (pullList.get(k).get(0).getAmt() == 0) pullList.get(k).remove(0);
                        }
                    }
                }
        ));
    }

    public static Map<String, List<ResourceElement>> deepCloneResourceMap(Map<String, List<ResourceElement>> resourceElementMap) {
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
