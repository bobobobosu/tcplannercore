package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.datastructure.LocationHierarchyMap;
import bo.tc.tcplanner.datastructure.ResourceElement;
import bo.tc.tcplanner.domain.Allocation;
import com.google.common.collect.Range;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.kie.api.definition.rule.All;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    static class ResourceChangeChain {
        List<Map<String, List<ResourceElement>>> deltaChain = new ArrayList<>();
        List<Map<String, List<ResourceElement>>> absChain = new ArrayList<>();
        List<Map<String, List<ResourceElement>>> aliveChain = new ArrayList<>();
        List<ResourceElement> resourceElements = new ArrayList<>();
        Map<ResourceElement, Range<Integer>> resourceAliveMap = new IdentityHashMap<>();

        ResourceChangeChain(List<Allocation> focusedAllocationList) {
            focusedAllocationList.forEach(x -> {
                deltaChain.add(new HashMap<>());
                absChain.add(new HashMap<>());
                aliveChain.add(new HashMap<>());
            });
            for (int i = 0; i < focusedAllocationList.size(); i++) {
                Allocation allocation = focusedAllocationList.get(i);
                List<Map<String, List<ResourceElement>>> forwardList = absChain.subList(i, absChain.size());
                List<Map<String, List<ResourceElement>>> backwardList = absChain.subList(0, i + 1);
                int finalI = i;
                allocation.getExecutionMode().getResourceStateChange().getResourceChange()
                        .forEach((k, v) -> v.forEach(x -> {
                                    ResourceElement resourceElement = new ResourceElement(x)
                                            .setPriorityTimelineIdList(allocation.getJob().getTimelineProperty().getDependencyIdList());
                                    resourceElements.add(resourceElement);

                                    if (resourceElement.getAmt() > 0) {
                                        if (!deltaChain.get(finalI).containsKey(k))
                                            deltaChain.get(finalI).put(k, new ArrayList<>());
                                        deltaChain.get(finalI).get(k).add(resourceElement);
                                        forwardList.forEach(eachMap -> {
                                            if (!eachMap.containsKey(k)) eachMap.put(k, new ArrayList<>());
                                            eachMap.get(k).add(resourceElement);
                                        });
                                        resourceAliveMap.put(resourceElement, Range.closed(finalI, focusedAllocationList.size() - 1));
                                    }
                                    if (resourceElement.getAmt() < 0) {
                                        if (!deltaChain.get(finalI).containsKey(k))
                                            deltaChain.get(finalI).put(k, new ArrayList<>());
                                        deltaChain.get(finalI).get(k).add(resourceElement);
                                        backwardList.forEach(eachMap -> {
                                            if (!eachMap.containsKey(k)) eachMap.put(k, new ArrayList<>());
                                            eachMap.get(k).add(resourceElement);
                                        });
                                        resourceAliveMap.put(resourceElement, Range.closed(0, finalI));
                                    }
                                }
                        ));
            }
            int g = 0;
        }
    }

    public static void updateAllocationResourceStateChange(List<Allocation> focusedAllocationList) {
        // Accumulate
        focusedAllocationList.forEach(x -> x.setResourceElementMap(new HashMap<>()));
        ResourceChangeChain resourceChangeChain = new ResourceChangeChain(focusedAllocationList);


        // Pull
        for (int i = 0; i < focusedAllocationList.size(); i++) {
            Map<String, List<ResourceElement>> absResourceMap = resourceChangeChain.absChain.get(i);
            Map<String, List<ResourceElement>> deltaResourceMap = resourceChangeChain.deltaChain.get(i);
            Map<ResourceElement, Range<Integer>> resourceAliveMap = resourceChangeChain.resourceAliveMap;
            // Clean
            absResourceMap.forEach((k, v) -> v.removeIf(x -> x.getAmt() == 0));
            absResourceMap.entrySet().removeIf(x -> x.getValue().size() == 0);

            // Build Pull Queue
            Map<String, List<ResourceElement>> pullList = absResourceMap
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            x -> x.getValue().stream().filter(y -> y.getAmt() < 0).collect(Collectors.toList())));

            // Sort Pull Queue
            Integer thisTimelineId = focusedAllocationList.get(i).getJob().getTimelineProperty().getTimelineid();
            pullList.forEach((k, v) -> v.sort(
                    (o1, o2) -> {
                        return new CompareToBuilder()
//                                .append(o1.getPriorityTimelineIdList().contains(thisTimelineId),
//                                        o2.getPriorityTimelineIdList().contains(thisTimelineId))
//                                .append(o1.getPriorityTimelineIdList().size(), o2.getPriorityTimelineIdList().size())
//                                .append(locationRestrictionCheck(o2.getLocation(), o1.getLocation()),
//                                        locationRestrictionCheck(o1.getLocation(), o2.getLocation()))
                                .append(resourceAliveMap.get(o1).lowerEndpoint(), resourceAliveMap.get(o2).lowerEndpoint())
//                                .append(o1.getAmt(), o2.getAmt())
                                .toComparison();
                    }
            ));

            // Pull
            absResourceMap.forEach((k, v) -> v.forEach(
                    x -> {
                        if (pullList.containsKey(k) && x.getAmt() > 0) {
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
                                    IntStream.range(resourceAliveMap.get(x).lowerEndpoint(),
                                            resourceAliveMap.get(nextResourceElement).upperEndpoint())
                                            .boxed()
                                            .collect(Collectors.toList())
                                            .forEach(integer -> {
                                                Map<String, List<ResourceElement>> targetMap =
                                                        resourceChangeChain.aliveChain.get(integer);
                                                if (!targetMap.containsKey(k)) targetMap.put(k, new ArrayList<>());
                                                targetMap.get(k).add(new ResourceElement(x).setAmt(delta));
                                            });

                                    nextResourceElement.setAmt(nextResourceElement.getAmt() + delta);
                                    changing = true;
                                }

                                if (pullList.get(k).get(0).getAmt() == 0) pullList.get(k).remove(0);
                            }
                        }
                    }
            ));

            // Clean
            absResourceMap.forEach((k, v) -> v.removeIf(x -> x.getAmt() == 0));

            // Clean
            deltaResourceMap.forEach((k, v) -> v.removeIf(x -> x.getAmt() == 0));
            deltaResourceMap.entrySet().removeIf(x -> x.getValue().size() == 0);
            absResourceMap.forEach((k, v) -> v.removeIf(x -> x.getAmt() == 0));
            absResourceMap.entrySet().removeIf(x -> x.getValue().size() == 0);

        }


        // Apply
        for (int i = 0; i < focusedAllocationList.size(); i++) {
            Allocation allocation = focusedAllocationList.get(i);
            Map<String, List<ResourceElement>> absResourceMap = resourceChangeChain.absChain.get(i);
            Map<String, List<ResourceElement>> aliveResourceMap = resourceChangeChain.aliveChain.get(i);
            Map<String, List<ResourceElement>> deltaResourceMap = resourceChangeChain.deltaChain.get(i);
            Map<String, List<ResourceElement>> resultResourceMap = new HashMap<>();
            aliveResourceMap.forEach((k, v) -> {
                if (!allocation.getExecutionMode().getResourceStateChange().getResourceChange().containsKey(k)) return;
                v.forEach(x -> {
                    if (x.getAmt() > 0) {
                        if (!resultResourceMap.containsKey(k)) resultResourceMap.put(k, new ArrayList<>());
                        resultResourceMap.get(k).add(x);
                    }
                });
            });
            absResourceMap.forEach((k, v) -> {
                if (!allocation.getExecutionMode().getResourceStateChange().getResourceChange().containsKey(k)) return;
                v.forEach(x -> {
                    if (x.getAmt() > 0) {
                        if (!resultResourceMap.containsKey(k)) resultResourceMap.put(k, new ArrayList<>());
                        resultResourceMap.get(k).add(x);
                    }
                });
            });
            deltaResourceMap.forEach((k, v) -> {
                if (!allocation.getExecutionMode().getResourceStateChange().getResourceChange().containsKey(k)) return;
                v.forEach(x -> {
                    if (x.getAmt() < 0) {
                        if (!resultResourceMap.containsKey(k)) resultResourceMap.put(k, new ArrayList<>());
                        resultResourceMap.get(k).add(x);
                    }
                });
            });
            focusedAllocationList.get(i).setResourceElementMap(resultResourceMap);
        }


//        // Clean
//        focusedAllocationList.forEach(
//                x -> {
//                    x.getResourceElementMap()
//                            .forEach((key, value) -> value.removeIf(z -> z.getAmt() == 0));
//                    x.getResourceElementMap().entrySet().removeIf(y -> y.getValue().size() == 0);
//                }
//        );


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
