package bo.tc.tcplanner.datastructure.converters;

import bo.tc.tcplanner.datastructure.*;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import com.google.common.collect.Lists;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static bo.tc.tcplanner.app.TCSchedulingApp.dtf_TimelineEntry;
import static bo.tc.tcplanner.app.Toolbox.OffsetMinutes2ZonedDatetime;
import static bo.tc.tcplanner.app.Toolbox.jacksonDeepCopy;
import static bo.tc.tcplanner.datastructure.converters.DataStructureBuilder.deletedRownum;

public class DataStructureWriter {
    public TimelineBlock generateTimelineBlock(TimelineBlock oldTimelineBlock, Schedule result) {
        oldTimelineBlock = (TimelineBlock) jacksonDeepCopy(oldTimelineBlock);


        TimelineBlock timelineBlock = new TimelineBlock()
                .setBlockStartTime(oldTimelineBlock.getBlockStartTime())
                .setBlockEndTime(oldTimelineBlock.getBlockEndTime())
                .setBlockStartRow(oldTimelineBlock.getBlockStartRow())
                .setBlockEndRow(oldTimelineBlock.getBlockEndRow())
                .setBlockScheduleAfter(oldTimelineBlock.getBlockScheduleAfter())
                .setOrigin("tcplannercore");

        // Previous Settings
        ZoneId zoneId = ZoneId.systemDefault();

        HashMap<Integer, TimelineEntry> id2timelineEntryMap = new HashMap<>();
        for (TimelineEntry timelineEntry : oldTimelineBlock.getTimelineEntryList())
            id2timelineEntryMap.put(timelineEntry.getId(), timelineEntry);

        // create result TimelineEntry
        List<TimelineEntry> TEList = new ArrayList<>();
        for (Allocation allocation : result.getAllocationList()) {
            if (allocation.getJob().isVolatileFlag() || allocation.getExecutionMode().isVolatileFlag() || allocation.isVolatileFlag())
                continue;

            // Initialize
            TimelineEntry TE = new TimelineEntry();
            if (allocation.getJob().getTimelineid() != null) {
                TE.setId(allocation.getJob().getTimelineid());
            } else {
                TE.setId(newID(TEList));
            }

            // Explore Previous Settings
            if (id2timelineEntryMap.containsKey(TE.getId()))
                zoneId = ZonedDateTime.parse(id2timelineEntryMap.get(TE.getId()).getStartTime()).getZone();

            // Basic property
            if (allocation.getJob().getRownum() != null)
                TE.setRownum(allocation.getJob().getRownum());
            TE.setTitle(allocation.getJob().getName())
                    .setDescription(allocation.getJob().getDescription())
                    .setStartTime(OffsetMinutes2ZonedDatetime(allocation.getProject().getSchedule().getGlobalStartTime(),
                            allocation.getStartDate()).withZoneSameInstant(zoneId).format(dtf_TimelineEntry))
                    .setTaskMode(allocation.getExecutionMode().getExecutionModeIndex());

            if (allocation.getExecutionMode().getJob().getDeadline() != null)
                TE.setDeadline(OffsetMinutes2ZonedDatetime(allocation.getProject().getSchedule().getGlobalStartTime(),
                        allocation.getExecutionMode().getJob().getDeadline()).withZoneSameInstant(zoneId)
                        .format(dtf_TimelineEntry));

            // Chronological Property
            TE.setMovable(allocation.getJob().getMovable())
                    .setSplittable(allocation.getJob().getSplittable())
                    .setChangeable(allocation.getJob().getChangeable())
                    .setDependencyIdList(allocation.getJob().getDependencyTimelineIdList());

            //Progress Change
            TE.setProgressChange(new ProgressChange());
            TE.getProgressChange().setProgressDelta((double) allocation.getProgressdelta() / 100);
            if (allocation.getProgressdelta() == 0) TE.setRownum(deletedRownum);

            // Resource State Change
            TE.setResourceStateChange(new ResourceStateChange().setResourceChange(
                    allocation.getExecutionMode().getResourceStateChange().getResourceChange()
                            .entrySet()
                            .stream()
                            .filter(a -> !a.getValue().isVolatileFlag())
                            .collect(Collectors.toMap(a -> a.getKey(), a -> a.getValue()))
            ));

            // Human State Change
            TE.setHumanStateChange(new HumanStateChange()
                    .setCurrentLocation(allocation.getExecutionMode().getCurrentLocation())
                    .setMovetoLocation(allocation.getExecutionMode().getMovetoLocation())
                    .setDuration(allocation.getPlannedDuration())
                    .setRequirementTimerange(allocation.getExecutionMode().getHumanStateChange().getRequirementTimerange()));

            TEList.add(TE);
        }

        // Build Rownum
        List<Integer> rownumList = new ArrayList<>();
        for (TimelineEntry timelineEntry : TEList) {
            if (timelineEntry.getRownum() != null) {
                rownumList.add(timelineEntry.getRownum());
            }
        }
        Collections.sort(rownumList);
        ListIterator<Integer> rownumIterator = rownumList.listIterator();
        int tmprownum = rownumList.get(0);
        for (TimelineEntry timelineEntry : TEList) {
            if (timelineEntry.getRownum() != null && rownumIterator.hasNext()) {
                tmprownum = rownumIterator.next();
            }
            timelineEntry.setRownum(tmprownum);
        }

        // Add deleted
        Map<Integer, TimelineEntry> timelineid2timelineEntryMap = new HashMap<>();
        for (TimelineEntry timelineEntry : TEList) {
            timelineid2timelineEntryMap.put(timelineEntry.getId(), timelineEntry);
        }
        for (TimelineEntry timelineEntry : oldTimelineBlock.getTimelineEntryList()) {
            if (!timelineid2timelineEntryMap.containsKey(timelineEntry.getId())) {
                timelineEntry.setRownum(deletedRownum);
                TEList.add(timelineEntry);
            }
        }

        timelineBlock.setTimelineEntryList(TEList);
        int percentComplete = 100 * (TEList.get(TEList.size() - 1).getRownum() - result.getGlobalStartRow()) / (result.getGlobalEndRow() - result.getGlobalStartRow());
        timelineBlock.setScore(TEList.get(TEList.size() - 1).getRownum() + "(" + percentComplete + "%)" +
                (result.getScore() != null ? result.getScore().toShortString() : ""));

        return timelineBlock;
    }

    public Integer newID(List<TimelineEntry> timelineEntryList) {
        HashMap<Integer, TimelineEntry> id2timelineEntryMap = new HashMap<>();
        for (TimelineEntry timelineEntry : timelineEntryList)
            id2timelineEntryMap.put(timelineEntry.getId(), timelineEntry);
        boolean found = false;
        Integer tmpId = -1;
        while (!found) {
            if (!id2timelineEntryMap.containsKey(tmpId)) {
                return tmpId;
            } else {
                tmpId--;
            }
        }
        return tmpId;
    }
}
