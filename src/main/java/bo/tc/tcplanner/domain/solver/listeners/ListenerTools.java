package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.datastructure.ResourceElement;
import bo.tc.tcplanner.datastructure.ResourceElementMap;
import bo.tc.tcplanner.domain.Allocation;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static bo.tc.tcplanner.app.DroolsTools.locationRestrictionCheck;
import static bo.tc.tcplanner.app.TCSchedulingApp.locationHierarchyMap;

public class ListenerTools {
    public static boolean updatePlanningDuration(Allocation allocation) {
        Duration duration = Duration.ofMinutes(
                (long) (allocation.getTimelineEntry().getHumanStateChange().getDuration()
                        * (allocation.getProgressdelta()) / (100)));
        boolean changed = allocation.getPlannedDuration() == null || allocation.getPlannedDuration().equals(duration);
        allocation.setPlannedDuration(duration);
        return changed;
    }

    public static boolean updatePredecessorsDoneDate(Allocation allocation, Allocation prevAllocation) {
        var endDate = prevAllocation.getEndDate();
        try {
            boolean f = allocation.getPredecessorsDoneDate() != null && allocation.getPredecessorsDoneDate().isEqual(endDate);
        }catch (Exception ex){
            int g=0;
        }
        boolean changed = allocation.getPredecessorsDoneDate() == null ||
                !allocation.getPredecessorsDoneDate().isEqual(endDate);
        allocation.setPredecessorsDoneDate(endDate);
        return changed;
    }

    public static boolean updateAllocationPreviousStandstill(Allocation allocation, Allocation prevAllocation) {
        String PreviousStandStill = prevAllocation.getPreviousStandstill();

        if (!locationHierarchyMap.containsKey(PreviousStandStill) ||
                !locationHierarchyMap.get(PreviousStandStill).contains(
                        prevAllocation.getTimelineEntry().getHumanStateChange().getCurrentLocation())) {
            PreviousStandStill = prevAllocation.getTimelineEntry().getHumanStateChange().getCurrentLocation();
        }

        if (!locationHierarchyMap.containsKey(PreviousStandStill) ||
                !locationHierarchyMap.get(PreviousStandStill).contains(
                        prevAllocation.getTimelineEntry().getHumanStateChange().getMovetoLocation())) {
            PreviousStandStill = prevAllocation.getTimelineEntry().getHumanStateChange().getMovetoLocation();
        }

        boolean changed = allocation.getPreviousStandstill() == null ||
                !allocation.getPreviousStandstill().equals(PreviousStandStill);
        allocation.setPreviousStandstill(PreviousStandStill);
        return changed;
    }

    public static List<ResourceElementMap> updateAllocationResourceStateChange(List<Allocation> focusedAllocationList, Set<String> dirty) {
        ResourceChangeChain resourceChangeChain = new ResourceChangeChain(focusedAllocationList, dirty);

        // Pull
        for (int i = 0; i < focusedAllocationList.size(); i++) {
            int finalI = i;
            Allocation allocation = focusedAllocationList.get(i);
            Map<ResourceElement, Integer> resourceSourceMap = resourceChangeChain.resourceSourceMap;
            Map<String, List<ResourceElement>> pushpullList = new TreeMap<>();
            allocation.getTimelineEntry().getResourceStateChange().getResourceChange().forEach((k, v) -> {
                if (resourceChangeChain.pushpullMap.get(finalI).containsKey(k))
                    pushpullList.put(k, resourceChangeChain.pushpullMap.get(finalI).get(k));
            });

            pushpullList.forEach((k, v) -> {
                v.sort(
                        (o1, o2) -> pullOrderCompareToBuilder(o1, o2, resourceSourceMap, allocation.getTimelineEntry().getTimelineProperty().getTimelineid())
                );

                int posIdx = -1;
                int negIdx = -1;
                while ((posIdx = nextPositive(v, posIdx)) != -1) {
                    ResourceElement thisResourceElement = v.get(posIdx);
                    if ((negIdx == -1) || (v.get(negIdx).getAmt() == 0)) negIdx = nextNegative(v, negIdx);
                    int thisNegIdx = negIdx;
                    while (thisNegIdx != -1) {
                        double delta;
                        ResourceElement nextResourceElement = v.get(negIdx);

                        // TODO locationRestrictionCheck
                        if ((locationRestrictionCheck(thisResourceElement.getLocation(), "") || true) &&
                                (delta = Math.min(thisResourceElement.getAmt(), -nextResourceElement.getAmt())) > 0) {
                            // valid push
                            thisResourceElement.setAmt(thisResourceElement.getAmt() - delta);
                            nextResourceElement.setAmt(nextResourceElement.getAmt() + delta);

                            // update applied timelineid
                            nextResourceElement.getAppliedTimelineIdList().add(
                                    focusedAllocationList.get(resourceSourceMap.get(thisResourceElement))
                            );

                            // save annihilated resource for checking capacity violations
                            IntStream.rangeClosed(
                                    resourceSourceMap.get(thisResourceElement),
                                    resourceSourceMap.get(nextResourceElement))
                                    .forEach(x -> {
                                        if (focusedAllocationList.get(x).getTimelineEntry().getResourceStateChange().getResourceChange().containsKey(k))
                                            addResourceElement(
                                                    resourceChangeChain.resultChain.get(x),
                                                    k,
                                                    new ResourceElement(thisResourceElement).setAmt(delta));
                                    });

                        }

                        thisNegIdx = nextNegative(v, thisNegIdx);
                    }
                }
            });
        }
        // Apply
        for (int i = 0; i < focusedAllocationList.size(); i++) {
            if (focusedAllocationList.get(i).getResourceElementMap() != null) {
                for (var entry : focusedAllocationList.get(i).getResourceElementMap().entrySet()) {
                    if (!dirty.contains(entry.getKey())) {
                        resourceChangeChain.resultChain.get(i).put(entry.getKey(), entry.getValue());
                    }
                }
            }

        }

        return resourceChangeChain.resultChain;
    }


    static class ResourceChangeChain {
        List<ResourceElementMap> resultChain = new ArrayList<>();
        Map<Integer, ResourceElementMap> pushpullMap = new HashMap<>();
        Map<ResourceElement, Integer> resourceSourceMap = new IdentityHashMap<>();

        ResourceChangeChain(List<Allocation> focusedAllocationList, Set<String> dirty) {
            // initialization
            for (int i = 0; i < focusedAllocationList.size(); i++) {
                pushpullMap.put(i, new ResourceElementMap());
                resultChain.add( new ResourceElementMap());
            }

            for (int i = 0; i < focusedAllocationList.size(); i++) {
                Allocation allocation = focusedAllocationList.get(i);
                int finalI = i;
                dirty.forEach(k -> {
                    Map<String, List<ResourceElement>> resourceChange = allocation.getTimelineEntry().getResourceStateChange().getResourceChange();
                    if (!resourceChange.containsKey(k))
                        return;
                    resourceChange.get(k).forEach(x -> {
                        // create resourceElement
                        ResourceElement resourceElement = new ResourceElement(x)
                                // TODO this line breaks full assert
                                .setAmt(x.getAmt() *
                                        (allocation.getProgressdelta().doubleValue() /
                                                (100 * allocation.getTimelineEntry().getProgressChange().getProgressDelta())))
                                .setAppliedTimelineIdList(new TreeSet<>())
                                .setPriorityTimelineIdList(
                                        allocation.getTimelineEntry().getTimelineProperty().getDependencyIdList());

                        // populate resource source Map
                        resourceSourceMap.put(resourceElement, finalI);

                        // populate resultChain and pushpullMap
                        List<Integer> applicableList = new ArrayList<>();
                        for (int j = 0; j < focusedAllocationList.size(); j++) {
                            Allocation allocation1 = focusedAllocationList.get(j);
                            if (!focusedAllocationList.get(j).getTimelineEntry().getResourceStateChange()
                                    .getResourceChange().containsKey(k)) continue;
                            boolean isAbs = allocation1.getTimelineEntry().getResourceStateChange().getMode()
                                    .equals(PropertyConstants.ResourceStateChangeTypes.types.absolute.name());
                            if (j <= finalI && isAbs) applicableList.clear();
                            if (j > finalI && isAbs) break;
                            applicableList.add(j);
                        }
                        for (int j : applicableList) {
                            if (j <= finalI && x.getAmt() <= 0)
                                addResourceElement(pushpullMap.get(j), k, resourceElement);
                            if (j == finalI && x.getAmt() <= 0)
                                addResourceElement(resultChain.get(j), k, resourceElement);
                            if (j >= finalI && x.getAmt() > 0) {
                                addResourceElement(resultChain.get(j), k, resourceElement);
                                addResourceElement(pushpullMap.get(j), k, resourceElement);
                            }
                        }
                    });
                });
            }
        }
    }

    private static int pullOrderCompareToBuilder(ResourceElement o1, ResourceElement o2,
                                                 Map<ResourceElement, Integer> resourceSourceMap,
                                                 Integer timelineId) {
        if (timelineId != null) {
            return new CompareToBuilder()
                    .append(o2.getPriorityTimelineIdList().contains(timelineId),
                            o1.getPriorityTimelineIdList().contains(timelineId))
//                                .append(locationRestrictionCheck(o2.getLocation(), o1.getLocation()),
//                                        locationRestrictionCheck(o1.getLocation(), o2.getLocation()))
                    .append(resourceSourceMap.get(o1), resourceSourceMap.get(o2))
                    .append(o1.getPriorityTimelineIdList().size(), o2.getPriorityTimelineIdList().size())
                    .append(o1.getAmt(), o2.getAmt())
                    .toComparison();
        } else {
            return new CompareToBuilder()
//                                .append(locationRestrictionCheck(o2.getLocation(), o1.getLocation()),
//                                        locationRestrictionCheck(o1.getLocation(), o2.getLocation()))
                    .append(resourceSourceMap.get(o1), resourceSourceMap.get(o2))
                    .append(o1.getPriorityTimelineIdList().size(), o2.getPriorityTimelineIdList().size())
                    .append(o1.getAmt(), o2.getAmt())
                    .toComparison();
        }
    }

    private static void addResourceElement(Map<String, List<ResourceElement>> resourceElementMap, String key, ResourceElement resourceElement) {
        Map<String, List<ResourceElement>> targetMap = resourceElementMap;
        if (!targetMap.containsKey(key)) targetMap.put(key, new ArrayList<>());
        targetMap.get(key).add(resourceElement);
    }

    private static int nextPositive(List<ResourceElement> resourceElementList, int startIdx) {
        while ((startIdx = startIdx + 1) < resourceElementList.size()) {
            if (resourceElementList.get(startIdx).getAmt() > 0) break;
        }
        return startIdx < resourceElementList.size() ? startIdx : -1;
    }

    private static int nextNegative(List<ResourceElement> resourceElementList, int startIdx) {
        while ((startIdx = startIdx + 1) < resourceElementList.size()) {
            if (resourceElementList.get(startIdx).getAmt() < 0) break;
        }
        return startIdx < resourceElementList.size() ? startIdx : -1;
    }

    public static Map<String, List<ResourceElement>> deepCloneResourceMap
            (Map<String, List<ResourceElement>> resourceElementMap) {
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
