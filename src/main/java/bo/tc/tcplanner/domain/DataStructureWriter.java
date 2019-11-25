package bo.tc.tcplanner.domain;

import bo.tc.tcplanner.datastructure.HumanStateChange;
import bo.tc.tcplanner.datastructure.ProgressChange;
import bo.tc.tcplanner.datastructure.TimelineBlock;
import bo.tc.tcplanner.datastructure.TimelineEntry;
import com.google.common.collect.Lists;
import org.kie.api.definition.rule.All;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static bo.tc.tcplanner.app.TCSchedulingApp.dtf_TimelineEntry;
import static bo.tc.tcplanner.app.Toolbox.OffsetMinutes2ZonedDatetime;
import static bo.tc.tcplanner.domain.solver.filters.FilterTools.isNotMovable;

public class DataStructureWriter {
    public TimelineBlock generateTimelineBlock(TimelineBlock oldTimelineBlock, Schedule result) {
        TimelineBlock timelineBlock = new TimelineBlock();
        timelineBlock.setBlockStartTime(oldTimelineBlock.getBlockStartTime());
        timelineBlock.setBlockEndTime(oldTimelineBlock.getBlockEndTime());
        timelineBlock.setBlockStartRow(oldTimelineBlock.getBlockStartRow());
        timelineBlock.setBlockEndRow(oldTimelineBlock.getBlockEndRow());
        timelineBlock.setBlockScheduleAfter(oldTimelineBlock.getBlockScheduleAfter());
        timelineBlock.setOrigin("tcplannercore");

        // Previous Settings
        ZoneId zoneId = ZoneId.systemDefault();

        HashMap<Integer, TimelineEntry> id2timelineEntryMap = new HashMap<>();
        for (TimelineEntry timelineEntry : oldTimelineBlock.getTimelineEntryList())
            id2timelineEntryMap.put(timelineEntry.getId(), timelineEntry);

        // create result TimelineEntry
        List<TimelineEntry> TEList = new ArrayList<>();
        for (Allocation allocation : result.getAllocationList()) {
            if(!TimelineBlockFilter(allocation)) continue;

            TimelineEntry TE = new TimelineEntry();
            if (allocation.getJob().getTimelineid() != null) {
                TE.setId(allocation.getJob().getTimelineid());
            } else {
                TE.setId(newID(TEList));
            }

            // Explore Previous Settings
            if (id2timelineEntryMap.containsKey(TE.getId()))
                zoneId = ZonedDateTime.parse(id2timelineEntryMap.get(TE.getId()).getStartTime()).getZone();

            //Progress Change
            TE.setProgressChange(new ProgressChange());
            TE.getProgressChange().setProgressDelta((double) allocation.getProgressdelta() / 100);
            if (allocation.getProgressdelta() == 0) TE.setRownum(999999);

            // Write New Settings
            if (allocation.getJob().getRownum() != null)
                TE.setRownum(allocation.getJob().getRownum());
            TE.setTitle(allocation.getJob().getName());
            TE.setDescription(allocation.getJob().getDescription());
            TE.setHumanStateChange(new HumanStateChange(allocation.getExecutionMode().getCurrentLocation(),
                    allocation.getExecutionMode().getMovetoLocation(), allocation.getPlannedDuration()));
            TE.getHumanStateChange().setRequirementTimerange(
                    allocation.getExecutionMode().getHumanStateChange().getRequirementTimerange());

            if (allocation.getExecutionMode().getJob().getDeadline() != null)
                TE.setDeadline(OffsetMinutes2ZonedDatetime(allocation.getProject().getSchedule().getGlobalStartTime(),
                        allocation.getExecutionMode().getJob().getDeadline()).withZoneSameInstant(zoneId)
                        .format(dtf_TimelineEntry));
            TE.setStartTime(OffsetMinutes2ZonedDatetime(allocation.getProject().getSchedule().getGlobalStartTime(),
                    allocation.getStartDate()).withZoneSameInstant(zoneId).format(dtf_TimelineEntry));
            TE.setTaskMode(allocation.getExecutionMode().getExecutionModeIndex());
            TE.setMovable(allocation.getJob().getMovable());
            TE.setSplittable(allocation.getJob().getSplittable());
            List<Integer> dependencyJobList = new ArrayList<>();
            TE.setDependencyIdList(dependencyJobList);
            TE.setResourceStateChange(allocation.getExecutionMode().getResourceStateChange());
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
        Collections.reverse(rownumList);
        ListIterator<Integer> rownumIterator = rownumList.listIterator();
        int tmprownum = 99999999;
        for (TimelineEntry timelineEntry : Lists.reverse(TEList)) {
            if (timelineEntry.getRownum() != null) {
                tmprownum = rownumIterator.next();
            }
            timelineEntry.setRownum(tmprownum);
        }

        // for(TimelineEntry te : TEList){
        // System.out.println(te.getId() + " " + te.getStartTime());
        // }
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

    public boolean TimelineBlockFilter(Allocation allocation) {
        if (allocation.getJob() == DataStructureBuilder.dummyJob) return false;
        if (allocation.getJob() == DataStructureBuilder.sourceJob) return false;
        if (allocation.getJob() == DataStructureBuilder.sinkJob) return false;
        return true;
    }
}
