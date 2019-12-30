package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.datastructure.ResourceElement;
import bo.tc.tcplanner.domain.Allocation;
import com.google.common.collect.Range;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static bo.tc.tcplanner.app.DroolsTools.locationRestrictionCheck;
import static bo.tc.tcplanner.app.TCSchedulingApp.locationHierarchyMap;

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

    static class ResourceChangeChain {
        List<Map<String, List<ResourceElement>>> resultChain = new ArrayList<>();
        Map<Integer, Map<String, List<ResourceElement>>> pushpullMap = new HashMap<>();
        Map<Integer, Map<String, List<ResourceElement>>> lockedResourceMap = new HashMap<>();
        Map<ResourceElement, Range<Integer>> resourceRangeMap = new IdentityHashMap<>();
        Map<ResourceElement, Integer> resourceSourceMap = new HashMap<>();

        ResourceChangeChain(List<Allocation> focusedAllocationList) {
            // initialization
            for (int i = 0; i < focusedAllocationList.size(); i++) {
                pushpullMap.put(i, new HashMap<>());
                lockedResourceMap.put(i, new HashMap<>());
                resultChain.add(new HashMap<>());
            }

            for (int i = 0; i < focusedAllocationList.size(); i++) {
                Allocation allocation = focusedAllocationList.get(i);
                int finalI = i;
                allocation.getExecutionMode().getResourceStateChange().getResourceChange()
                        .forEach((k, v) -> v.forEach(x -> {
                                    // create resourceElement
                                    ResourceElement resourceElement = new ResourceElement(x)
                                            .setAmt(x.getAmt() * allocation.getProgressdelta() * 0.01)
                                            .setPriorityTimelineIdList(allocation.getJob().getTimelineProperty().getDependencyIdList());

                                    // populate resultRangeMap
                                    Range<Integer> resourceRange = (resourceElement.getAmt() > 0) ?
                                            Range.closed(finalI, focusedAllocationList.size() - 1) :
                                            Range.closed(0, finalI);
                                    resourceRangeMap.put(resourceElement, resourceRange);
                                    IntStream.range(resourceRange.lowerEndpoint(), resourceRange.upperEndpoint() + 1)
                                            .forEach(y -> {
                                                Map<String, List<ResourceElement>> map = pushpullMap.get(y);
                                                if (!map.containsKey(k)) map.put(k, new ArrayList<>());
                                                map.get(k).add(resourceElement);
                                            });

                                    // populate resultChain
                                    if (!resultChain.get(finalI).containsKey(k))
                                        resultChain.get(finalI).put(k, new ArrayList<>());
                                    resultChain.get(finalI).get(k).add(resourceElement);

                                    // populate resource source Map
                                    resourceSourceMap.put(resourceElement, allocation.getJob().getTimelineProperty().getTimelineid());
                                }
                        ));
            }
            int g = 0;
        }

    }

    public static void updateAllocationResourceStateChange(List<Allocation> focusedAllocationList) {
        // Init
        focusedAllocationList.forEach(x -> x.setResourceElementMap(new HashMap<>()));
        ResourceChangeChain resourceChangeChain = new ResourceChangeChain(focusedAllocationList);

        // Pull
        for (int i = 0; i < focusedAllocationList.size(); i++) {
            Allocation allocation = focusedAllocationList.get(i);
            Map<ResourceElement, Range<Integer>> resourceRangeMap = resourceChangeChain.resourceRangeMap;

            // Build Pull Queue
            Map<String, List<ResourceElement>> pullList = resourceChangeChain.pushpullMap.get(i)
                    .entrySet()
                    .stream()
                    .filter(x -> allocation.getExecutionMode().getResourceStateChange().getResourceChange().containsKey(x.getKey()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            x -> x.getValue().stream().filter(y -> y.getAmt() < 0).collect(Collectors.toList())));

//            // Sort Pull Queue
//            Integer thisTimelineId = focusedAllocationList.get(i).getJob().getTimelineProperty().getTimelineid();
//            pullList.forEach((k, v) -> v.sort(
//                    (o1, o2) -> {
//                        return new CompareToBuilder()
////                                .append(o1.getPriorityTimelineIdList().contains(thisTimelineId),
////                                        o2.getPriorityTimelineIdList().contains(thisTimelineId))
////                                .append(o1.getPriorityTimelineIdList().size(), o2.getPriorityTimelineIdList().size())
////                                .append(locationRestrictionCheck(o2.getLocation(), o1.getLocation()),
////                                        locationRestrictionCheck(o1.getLocation(), o2.getLocation()))
//                                .append(resourceRangeMap.get(o1).lowerEndpoint(), resourceRangeMap.get(o2).lowerEndpoint())
////                                .append(o1.getAmt(), o2.getAmt())
//                                .toComparison();
//                    }
//            ));

            // Build push Queue
            Map<String, List<ResourceElement>> pushList = resourceChangeChain.pushpullMap.get(i)
                    .entrySet()
                    .stream()
                    .filter(x -> allocation.getExecutionMode().getResourceStateChange().getResourceChange().containsKey(x.getKey()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            x -> x.getValue().stream().filter(y -> y.getAmt() > 0).collect(Collectors.toList())));

            // Push
            pushList.forEach((k, v) -> v.forEach(
                    x -> {
                        if (pullList.containsKey(k)) {
                            boolean changing = true;
                            ResourceElement nextResourceElement;
                            while (changing &&
                                    pullList.get(k).size() > 0 &&
                                    (nextResourceElement = pullList.get(k).get(0)) != null) {
                                changing = false;
                                double delta;
                                // TODO
                                if ((locationRestrictionCheck(x.getLocation(), "") || true) &&
                                        (delta = Math.min(x.getAmt(), -nextResourceElement.getAmt())) > 0) {
                                    // valid push
                                    x.setAmt(x.getAmt() - delta);
                                    nextResourceElement.setAmt(nextResourceElement.getAmt() + delta);

                                    // save annihilated resource for checking capacity violations
                                    IntStream.range(resourceRangeMap.get(x).lowerEndpoint(),
                                            resourceRangeMap.get(nextResourceElement).upperEndpoint())
                                            .boxed()
                                            .collect(Collectors.toList())
                                            .forEach(integer -> {
                                                Map<String, List<ResourceElement>> targetMap =
                                                        resourceChangeChain.lockedResourceMap.get(integer);
                                                if (!targetMap.containsKey(k)) targetMap.put(k, new ArrayList<>());
                                                targetMap.get(k).add(new ResourceElement(x).setAmt(delta));
                                            });
                                    changing = true;
                                }

                                // drop empty resourceElement
                                if (pullList.get(k).get(0).getAmt() == 0) pullList.get(k).remove(0);
                            }
                        }
                    }
            ));

            // clean
            resourceChangeChain.pushpullMap.get(i).forEach((k, v) -> v.removeIf(x -> x.getAmt() == 0));
            resourceChangeChain.pushpullMap.get(i).entrySet().removeIf(x -> x.getValue().size() == 0);
        }


        // Apply
        for (int i = 0; i < focusedAllocationList.size(); i++) {
            Allocation allocation = focusedAllocationList.get(i);
            Map<String, List<ResourceElement>> result = new HashMap<>();

            // Negative Portion
            resourceChangeChain.resultChain.get(i).forEach((k, v) -> {
                if (!allocation.getExecutionMode().getResourceStateChange().getResourceChange().containsKey(k)) return;
                v.removeIf(x -> !(x.getAmt() < 0));
                if (!result.containsKey(k)) result.put(k, new ArrayList<>());
                result.get(k).addAll(v);
            });

            // Positive Portion
            resourceChangeChain.pushpullMap.get(i).forEach((k, v) -> {
                if (!allocation.getExecutionMode().getResourceStateChange().getResourceChange().containsKey(k)) return;
                v.removeIf(x -> !(x.getAmt() > 0));
                if (!result.containsKey(k)) result.put(k, new ArrayList<>());
                result.get(k).addAll(v);
            });
            resourceChangeChain.lockedResourceMap.get(i).forEach((k, v) -> {
                if (!allocation.getExecutionMode().getResourceStateChange().getResourceChange().containsKey(k)) return;
                v.removeIf(x -> !(x.getAmt() > 0));
                if (!result.containsKey(k)) result.put(k, new ArrayList<>());
                result.get(k).addAll(v);
            });

            focusedAllocationList.get(i).setResourceElementMap(result);
        }

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
