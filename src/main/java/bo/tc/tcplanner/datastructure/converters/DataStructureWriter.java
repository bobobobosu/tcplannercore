package bo.tc.tcplanner.datastructure.converters;

import bo.tc.tcplanner.datastructure.*;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.AllocationType;
import bo.tc.tcplanner.domain.ExecutionModeType;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static bo.tc.tcplanner.app.TCSchedulingApp.dtf_TimelineEntry;
import static bo.tc.tcplanner.app.Toolbox.*;
import static bo.tc.tcplanner.datastructure.converters.DataStructureBuilder.deletedRownum;

public class DataStructureWriter {
    public TimelineBlock generateTimelineBlock(Schedule result, Solver solver) {
        TimelineBlock timelineBlock = generateTimelineBlock(result);
        ScoreDirector<Schedule> scoreDirector = createScoreDirector(result);

        Map<Integer, Indictment> breakByTasks = new HashMap<>();
        for (Map.Entry<Object, Indictment> indictmentEntry : scoreDirector.getIndictmentMap().entrySet()) {
            if (indictmentEntry.getValue().getJustification() instanceof Allocation &&
                    Arrays.stream(((BendableScore) indictmentEntry.getValue().getScore()).getHardScores()).anyMatch(x -> x != 0)) {
                Allocation matchAllocation = (Allocation) indictmentEntry.getValue().getJustification();
                if (matchAllocation.getJob().getTimelineProperty().getTimelineid() != null)
                    breakByTasks.put(matchAllocation.getJob().getTimelineProperty().getTimelineid(), indictmentEntry.getValue());
            }
        }

        for (TimelineEntry timelineEntry : timelineBlock.getTimelineEntryList()) {
            timelineEntry.setScore(breakByTasks.containsKey(timelineEntry.getTimelineProperty().getTimelineid()) ?
                    hardConstraintMatchToString(breakByTasks.get(timelineEntry.getTimelineProperty().getTimelineid()).getConstraintMatchSet()) : "");
        }
        return timelineBlock;
    }

    public TimelineBlock generateTimelineBlock(Schedule result) {
        TimelineBlock oldTimelineBlock = (TimelineBlock) jacksonDeepCopy(result.getProblemTimelineBlock());

        TimelineBlock timelineBlock = new TimelineBlock()
                .setBlockStartTime(oldTimelineBlock.getBlockStartTime())
                .setBlockEndTime(oldTimelineBlock.getBlockEndTime())
                .setBlockStartRow(oldTimelineBlock.getBlockStartRow())
                .setBlockEndRow(oldTimelineBlock.getBlockEndRow())
                .setBlockScheduleAfter(oldTimelineBlock.getBlockScheduleAfter())
                .setOrigin("tcplannercore");

        // create result TimelineEntry
        List<TimelineEntry> TEList = new ArrayList<>();
        for (Allocation allocation : result.getAllocationList()) {
            if (allocation.getJob().isVolatileFlag() ||
                    allocation.getExecutionMode().isVolatileFlag() ||
                    allocation.isVolatileFlag() )
                continue;

            // Initialize
            TimelineEntry TE = new TimelineEntry();

            // Basic property
            TE.setTitle(allocation.getJob().getName())
                    .setDescription(allocation.getJob().getDescription())
                    .setExecutionMode(allocation.getExecutionMode().getExecutionModeIndex());

            // Chronological Property
            TE.setChronoProperty(new ChronoProperty(allocation.getExecutionMode().getChronoProperty())
                    .setStartTime(allocation.getStartDate().withZoneSameInstant(ZoneId.systemDefault()).format(dtf_TimelineEntry)));

            // Timeline Property Reset timelineid
            TE.setTimelineProperty(new TimelineProperty(allocation.getJob().getTimelineProperty()));
            if (TE.getTimelineProperty().getTimelineid() == null ||
                    allocation.getExecutionMode().getExecutionModeTypes().contains(ExecutionModeType.NEW)) {
                TE.getTimelineProperty().setTimelineid(newID(TEList));
            }

            // Progress Change
            TE.setProgressChange(new ProgressChange((double) allocation.getProgressdelta() / 100));

            // Resource State Change
            TE.setResourceStateChange(new ResourceStateChange(allocation.getExecutionMode().getResourceStateChange()));

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
        Set<Integer> remainingIdSet = TEList
                .stream()
                .map(x -> x.getTimelineProperty().getTimelineid())
                .collect(Collectors.toSet());
        for (TimelineEntry timelineEntry : oldTimelineBlock.getTimelineEntryList().stream()
                .filter(x -> !remainingIdSet.contains(x.getTimelineProperty().getTimelineid()))
                .collect(Collectors.toSet())) {
            TEList.add(timelineEntry.setTimelineProperty(
                    timelineEntry.getTimelineProperty().setRownum(deletedRownum)
            ));
        }


        timelineBlock.setTimelineEntryList(TEList);

        int percentComplete = 100 * (TEList.get(TEList.size() - 1).getTimelineProperty().getRownum() - result.getGlobalStartRow()) / (result.getGlobalEndRow() - result.getGlobalStartRow());
        timelineBlock.setScore(TEList.get(TEList.size() - 1).getTimelineProperty().getRownum() + "(" + percentComplete + "%)" +
                (result.getScore() != null ? result.getScore().toShortString() : ""));

        return timelineBlock;
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
