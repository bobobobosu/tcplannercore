package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.datastructure.ResourceElement;
import bo.tc.tcplanner.datastructure.ResourceElementMap;
import bo.tc.tcplanner.datastructure.TimelineEntry;
import bo.tc.tcplanner.domain.Allocation;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static bo.tc.tcplanner.app.DroolsTools.locationRestrictionCheck;
import static bo.tc.tcplanner.app.TCSchedulingApp.locationHierarchyMap;

public class ListenerTools {
    public static boolean updatePlanningDuration(Allocation allocation) {
        double multiplier = ((double) allocation.getProgressdelta() / 100) /
                (allocation.getTimelineEntry().getProgressChange().getProgressDelta());
        Duration duration = Duration.ofSeconds(
                (long) (allocation.getTimelineEntry().getHumanStateChange().getDuration() * 60 * multiplier));
        boolean changed = allocation.getPlannedDuration() == null || allocation.getPlannedDuration().equals(duration);
        allocation.setPlannedDuration(duration);
        return changed;
    }

    public static boolean updatePredecessorsDoneDate(Allocation allocation, Allocation prevAllocation) {
        var endDate = prevAllocation.getEndDate();
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
                        (o1, o2) -> pullOrderCompareToBuilder(o1, o2,
                                resourceSourceMap, focusedAllocationList,
                                allocation.getTimelineEntry())
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

                        // locationRestrictionCheck
                        if ((locationRestrictionCheck(thisResourceElement.getLocation(), "") || true) &&
                                (delta = Math.min(thisResourceElement.getAmt(), -nextResourceElement.getAmt())) > 0 &&
                                (thisResourceElement.getAmt() >= 1 || thisResourceElement.getSuppliedTimelineIdList().size() == 0)) {

                            // valid push
                            thisResourceElement.setAmt(thisResourceElement.getAmt() - delta);
                            nextResourceElement.setAmt(nextResourceElement.getAmt() + delta);

                            // ignore
                            thisResourceElement.setAmt(Math.abs(thisResourceElement.getAmt()) >= PropertyConstants.resourceIgnoreAmt ? thisResourceElement.getAmt() : 0);
                            nextResourceElement.setAmt(Math.abs(nextResourceElement.getAmt()) >= PropertyConstants.resourceIgnoreAmt ? nextResourceElement.getAmt() : 0);

                            // update applied timelineid
                            nextResourceElement.getDependedTimelineIdList().add(
                                    focusedAllocationList.get(resourceSourceMap.get(thisResourceElement))
                            );
                            thisResourceElement.getSuppliedTimelineIdList().add(
                                    focusedAllocationList.get(resourceSourceMap.get(nextResourceElement))
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
                resultChain.add(new ResourceElementMap());
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
                        double realAmt = x.getAmt() *
                                (allocation.getProgressdelta().doubleValue() /
                                        (100 * allocation.getTimelineEntry().getProgressChange().getProgressDelta()));
                        ResourceElement resourceElement = new ResourceElement(x)
                                .setAmt(!Double.isNaN(realAmt) ? realAmt : 0)
                                .setDependedTimelineIdList(new TreeSet<>())
                                .setSuppliedTimelineIdList(new TreeSet<>())
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
                                                 List<Allocation> allocationList,
                                                 TimelineEntry timelineEntry) {
        TimelineEntry o1TimelineEntry = allocationList.get(resourceSourceMap.get(o1)).getTimelineEntry();
        TimelineEntry o2TimelineEntry = allocationList.get(resourceSourceMap.get(o2)).getTimelineEntry();
        return new CompareToBuilder()
                .append(o1.getPriorityTimelineIdList().size(), o2.getPriorityTimelineIdList().size())
                .append(o2TimelineEntry.getDescription().equals(timelineEntry.getDescription()),
                        o1TimelineEntry.getDescription().equals(timelineEntry.getDescription()))
                .append(locationRestrictionCheck(o2.getLocation(), o1.getLocation()),
                        locationRestrictionCheck(o1.getLocation(), o2.getLocation()))
                .toComparison();

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
