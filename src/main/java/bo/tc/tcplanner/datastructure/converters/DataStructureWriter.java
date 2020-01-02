package bo.tc.tcplanner.datastructure.converters;

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.datastructure.*;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static bo.tc.tcplanner.app.TCSchedulingApp.dtf_TimelineEntry;
import static bo.tc.tcplanner.app.Toolbox.*;

public class DataStructureWriter {
    public TimelineBlock generateTimelineBlockScore(Schedule result) {
        TimelineBlock timelineBlock = generateTimelineBlock(result);
        ScoreDirector<Schedule> scoreDirector = createScoreDirector(result);

        Map<Integer, Indictment> breakByTasks = new HashMap<>();
        for (Map.Entry<Object, Indictment> indictmentEntry : scoreDirector.getIndictmentMap().entrySet()) {
            if (indictmentEntry.getValue().getJustification() instanceof Allocation &&
                    Arrays.stream(((BendableScore) indictmentEntry.getValue().getScore()).getHardScores()).anyMatch(x -> x != 0)) {
                Allocation matchAllocation = (Allocation) indictmentEntry.getValue().getJustification();
                if (matchAllocation.getTimelineEntry().getTimelineProperty().getTimelineid() != null)
                    breakByTasks.put(matchAllocation.getTimelineEntry().getTimelineProperty().getTimelineid(), indictmentEntry.getValue());
            }
        }

        for (TimelineEntry timelineEntry : timelineBlock.getTimelineEntryList()) {
            timelineEntry.setScore(breakByTasks.containsKey(timelineEntry.getTimelineProperty().getTimelineid()) ?
                    hardConstraintMatchToString(breakByTasks.get(timelineEntry.getTimelineProperty().getTimelineid()).getConstraintMatchSet()) : "");
        }
        return timelineBlock;
    }

    public TimelineBlock generateTimelineBlock(final Schedule result) {

        TimelineBlock oldTimelineBlock = (TimelineBlock) jacksonDeepCopy(result.getProblemTimelineBlock());

        TimelineBlock timelineBlock = new TimelineBlock()
                .setBlockStartTime(oldTimelineBlock.getBlockStartTime())
                .setBlockEndTime(oldTimelineBlock.getBlockEndTime())
                .setBlockStartRow(oldTimelineBlock.getBlockStartRow())
                .setBlockEndRow(oldTimelineBlock.getBlockEndRow())
                .setBlockScheduleAfter(oldTimelineBlock.getBlockScheduleAfter())
                .setOrigin("tcplannercore");

        // generate ids
        Map<Allocation, Integer> allocationRealidMap = new IdentityHashMap<>();
        Map<TimelineEntry, List<Allocation>> timelineEntryListMap =
                result.getAllocationList()
                        .stream()
                        .filter(x -> !x.isVolatileFlag())
                        .collect(Collectors.groupingBy(Allocation::getTimelineEntry));
        result.getAllocationList().stream().filter(x -> !x.isVolatileFlag()).forEach(x -> {
            if (x.getTimelineEntry().getTimelineProperty().getTimelineid() == null ||
                    x.getTimelineEntry().getTimelineProperty().getPlanningWindowType().equals(
                            PropertyConstants.PlanningWindowTypes.types.Draft.name())) {
                allocationRealidMap.put(x, newID(allocationRealidMap.values()));
            } else {
                allocationRealidMap.put(x, x.getTimelineEntry().getTimelineProperty().getTimelineid());
            }
        });


        // create result TimelineEntry
        List<TimelineEntry> TEList = new ArrayList<>();
        result.getAllocationList().stream().filter(x -> !x.isVolatileFlag()).forEach(allocation -> {
            // Initialize
            TimelineEntry TE = new TimelineEntry();

            // Basic property
            TE.setTitle(allocation.getTimelineEntry().getTitle())
                    .setDescription(allocation.getTimelineEntry().getDescription())
                    .setExecutionMode(allocation.getTimelineEntry().getExecutionMode());

            // Chronological Property
            TE.setChronoProperty(new ChronoProperty(allocation.getTimelineEntry().getChronoProperty())
                    .setStartTime(allocation.getStartDate().withZoneSameInstant(ZoneId.systemDefault()).format(dtf_TimelineEntry)));


            // Timeline Property Reset timelineid
            TE.setTimelineProperty(new TimelineProperty(allocation.getTimelineEntry().getTimelineProperty())
                    .setTimelineid(allocationRealidMap.get(allocation)));
            TE.getTimelineProperty().getDependencyIdList().addAll(allocation.getTimelineEntry().getResourceStateChange()
                    .getResourceChange().entrySet().stream()
                    .flatMap(
                            x -> allocation.getResourceElementMap().get(x.getKey()).stream()
                                    .filter(y -> y.getType().equals("requirement") && !y.isVolatileFlag())
                                    .flatMap(z -> z.getAppliedTimelineIdList().stream()
                                            .filter(w -> allocationRealidMap.containsKey(w) && !w.isVolatileFlag())
                                            .map(allocationRealidMap::get))
                    ).collect(Collectors.toSet()));
            TE.getTimelineProperty().getDependencyIdList().addAll(timelineEntryListMap
                    .get(allocation.getTimelineEntry())
                    .stream()
                    .filter(x -> x.getIndex() > allocation.getIndex() && !x.isVolatileFlag())
                    .map(allocationRealidMap::get)
                    .collect(Collectors.toList())
            );

            // Progress Change
            TE.setProgressChange(new ProgressChange().setProgressDelta((double) allocation.getProgressdelta() / 100));

            // Resource State Change
            TE.setResourceStateChange(new ResourceStateChange(allocation.getTimelineEntry().getResourceStateChange())
                    .setResourceStatus(allocation.getResourceElementMap().entrySet().stream().collect(
                            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                    .removeVolatile()
                    .removeEmpty());

            // Human State Change
            TE.setHumanStateChange(new HumanStateChange(allocation.getTimelineEntry().getHumanStateChange())
                    .setDuration(allocation.getPlannedDuration().toMinutes())
            );

            TEList.add(TE);
        });

        // Build Rownum
        Set<Integer> rownumList = TEList
                .stream()
                .map(x -> x.getTimelineProperty().getRownum())
                .collect(Collectors.toCollection(TreeSet::new));

        Iterator<Integer> rownumIterator = rownumList.iterator();
        int tmprownum = Collections.min(rownumList) - 1;

        for (TimelineEntry timelineEntry : TEList) {
            if (timelineEntry.getTimelineProperty().getTimelineid() > 0 && rownumIterator.hasNext()) {
                tmprownum = rownumIterator.next();
            }
            timelineEntry.getTimelineProperty().setRownum(tmprownum);
        }

        // Add deleted
        Set<Object> remainingIdSet = TEList
                .stream()
                .map(x -> x.getTimelineProperty().getTimelineid())
                .collect(Collectors.toSet());
        for (TimelineEntry timelineEntry : oldTimelineBlock.getTimelineEntryList().stream()
                .filter(x -> !remainingIdSet.contains(x.getTimelineProperty().getTimelineid()))
                .collect(Collectors.toSet())) {
            TEList.add(timelineEntry.setTimelineProperty(
                    timelineEntry.getTimelineProperty().setPlanningWindowType(PropertyConstants.PlanningWindowTypes.types.Deleted.name())
            ));
        }


        timelineBlock.setTimelineEntryList(TEList);

        int percentComplete = 100;//100 * (TEList.get(TEList.size() - 1).getTimelineProperty().getRownum() - result.getGlobalStartRow()) / (result.getGlobalEndRow() - result.getGlobalStartRow());
        timelineBlock.setScore(TEList.get(TEList.size() - 1).getTimelineProperty().getRownum() + "(" + percentComplete + "%)" +
                (result.getScore() != null ? result.getScore().toShortString() : ""));

        return timelineBlock;
    }

    public Integer newID(Collection<Integer> oldIds) {
        Integer tmpId = -1;
        while (true) {
            if (!oldIds.contains(tmpId)) {
                break;
            } else {
                tmpId--;
            }
        }
        return tmpId;
    }

    public Integer newID(List<TimelineEntry> timelineEntryList) {
        HashMap<Integer, TimelineEntry> id2timelineEntryMap = new HashMap<>();
        for (TimelineEntry timelineEntry : timelineEntryList)
            id2timelineEntryMap.put(timelineEntry.getTimelineProperty().getTimelineid(), timelineEntry);
        Integer tmpId = -1;
        while (true) {
            if (!id2timelineEntryMap.containsKey(tmpId)) {
                break;
            } else {
                tmpId--;
            }
        }
        return tmpId;
    }
}
