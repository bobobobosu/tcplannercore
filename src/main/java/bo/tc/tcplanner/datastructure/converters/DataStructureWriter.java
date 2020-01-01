package bo.tc.tcplanner.datastructure.converters;

import bo.tc.tcplanner.datastructure.*;
import bo.tc.tcplanner.domain.*;
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
                if (matchAllocation.getExecutionMode().getTimelineProperty().getTimelineid() != null)
                    breakByTasks.put(matchAllocation.getExecutionMode().getTimelineProperty().getTimelineid(), indictmentEntry.getValue());
            }
        }

        for (TimelineEntry timelineEntry : timelineBlock.getTimelineEntryList()) {
            timelineEntry.setScore(breakByTasks.containsKey(timelineEntry.getTimelineProperty().getTimelineid()) ?
                    hardConstraintMatchToString(breakByTasks.get(timelineEntry.getTimelineProperty().getTimelineid()).getConstraintMatchSet()) : "");
        }
        return timelineBlock;
    }

    public TimelineBlock generateTimelineBlock(Schedule oriresult) {
        Schedule result = new Schedule(oriresult);
        TimelineBlock oldTimelineBlock = (TimelineBlock) jacksonDeepCopy(result.getProblemTimelineBlock());

        TimelineBlock timelineBlock = new TimelineBlock()
                .setBlockStartTime(oldTimelineBlock.getBlockStartTime())
                .setBlockEndTime(oldTimelineBlock.getBlockEndTime())
                .setBlockStartRow(oldTimelineBlock.getBlockStartRow())
                .setBlockEndRow(oldTimelineBlock.getBlockEndRow())
                .setBlockScheduleAfter(oldTimelineBlock.getBlockScheduleAfter())
                .setOrigin("tcplannercore");

        result.removeVolatile();

        // generate ids
        Map<Allocation, Integer> allocationRealidMap = new IdentityHashMap<>();
        Map<ExecutionMode, List<Allocation>> executionModeListMap = result
                .getAllocationList()
                .stream()
                .collect(Collectors.groupingBy(Allocation::getExecutionMode));
        for (Allocation allocation : result.getAllocationList()) {
            if (allocation.getExecutionMode().getTimelineProperty().getTimelineid() == null ||
                    allocation.getExecutionMode().getExecutionModeTypes().contains(ExecutionModeType.NEW)) {
                allocationRealidMap.put(allocation, newID(allocationRealidMap.values()));
            } else {
                allocationRealidMap.put(allocation, allocation.getExecutionMode().getTimelineProperty().getTimelineid());
            }

        }

        // create result TimelineEntry
        List<TimelineEntry> TEList = new ArrayList<>();
        for (Allocation allocation : result.getAllocationList()) {
            // Initialize
            TimelineEntry TE = new TimelineEntry();

            // Basic property
            TE.setTitle(allocation.getExecutionMode().getTitle())
                    .setDescription(allocation.getExecutionMode().getDescription())
                    .setExecutionMode(allocation.getExecutionMode().getExecutionModeIndex());

            // Chronological Property
            TE.setChronoProperty(new ChronoProperty(allocation.getExecutionMode().getChronoProperty())
                    .setStartTime(allocation.getStartDate().withZoneSameInstant(ZoneId.systemDefault()).format(dtf_TimelineEntry)));


            // Timeline Property Reset timelineid
            TE.setTimelineProperty(new TimelineProperty(allocation.getExecutionMode().getTimelineProperty())
                    .setTimelineid(allocationRealidMap.get(allocation)));
            TE.getTimelineProperty().getDependencyIdList().addAll(allocation.getExecutionMode().getResourceStateChange()
                    .getResourceChange().entrySet().stream().flatMap(
                            x -> allocation.getResourceElementMap().get(x.getKey()).stream()
                                    .filter(y -> y.getType().equals("requirement"))
                                    .flatMap(z -> z.getAppliedTimelineIdList().stream()
                                            .filter(allocationRealidMap::containsKey)
                                            .map(allocationRealidMap::get))
                    ).collect(Collectors.toSet()));
            TE.getTimelineProperty().getDependencyIdList().addAll(executionModeListMap
                    .get(allocation.getExecutionMode())
                    .stream()
                    .filter(x -> x.getIndex() > allocation.getIndex())
                    .map(x -> allocationRealidMap.get(x))
                    .collect(Collectors.toList())
            );

            // Progress Change
            TE.setProgressChange(new ProgressChange().setProgressDelta((double) allocation.getProgressdelta() / 100));

            // Resource State Change
            TE.setResourceStateChange(new ResourceStateChange(allocation.getExecutionMode().getResourceStateChange())
                    .setResourceStatus(allocation.getResourceElementMap()));
            TE.getResourceStateChange().getResourceStatus().forEach((k, v) -> v.removeIf(x -> x.getAmt() == 0));
            TE.getResourceStateChange().getResourceStatus().entrySet().removeIf(x -> x.getValue().size() == 0);


            // Human State Change
            TE.setHumanStateChange(new HumanStateChange(allocation.getExecutionMode().getHumanStateChange())
                    .setDuration(allocation.getPlannedDuration().toMinutes())
            );

            TEList.add(TE);
        }

        // Build Rownum
        Set<Integer> rownumList = TEList
                .stream()
                .filter(x -> x.getTimelineProperty().getRownum() != null)
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
                    timelineEntry.getTimelineProperty().setDeleted(1)
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
