package bo.tc.tcplanner.datastructure.converters;

import bo.tc.tcplanner.datastructure.*;
import bo.tc.tcplanner.domain.*;
import bo.tc.tcplanner.domain.solver.listeners.ListenerTools;
import com.google.common.collect.Sets;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static bo.tc.tcplanner.app.DroolsTools.getConstrintedTimeRange;
import static bo.tc.tcplanner.domain.solver.filters.FilterTools.isNotChangeable;
import static bo.tc.tcplanner.domain.solver.listeners.ListenerTools.*;
import static java.lang.Math.round;


public class DataStructureBuilder {
    Schedule schedule;

    public DataStructureBuilder(ValueEntryMap valueEntryMap, TimelineBlock timelineBlock, HashMap<String, Object> timeHierarchyMap) {
        schedule = new Schedule()
                .setValueEntryMap(new ValueEntryMap(valueEntryMap))
                .setTimeEntryMap(new TimeEntryMap())
                .setProblemTimelineBlock(new TimelineBlock(timelineBlock));
        schedule.getTimeEntryMap().putAll(timeHierarchyMap.keySet().stream().collect(Collectors.toMap(
                x -> x,
                x -> getConstrintedTimeRange(timeHierarchyMap, x,
                        timelineBlock.getZonedBlockStartTime(),
                        timelineBlock.getZonedBlockEndTime())
        )));

        // Set Constants
        schedule.special.dummyLocation = "Undefined";
        schedule.special.dummyTime = "Anytime";
        schedule.special.dummyHumamStateChange = new HumanStateChange()
                .setDuration(0)
                .setCurrentLocation(schedule.special.dummyLocation)
                .setMovetoLocation(schedule.special.dummyLocation)
                .setRequirementTimerange(schedule.special.dummyTime);
        schedule.special.dummyProgressChange = new ProgressChange()
                .setProgressDelta(0.1);
        schedule.special.dummyResourceStateChange = new ResourceStateChange()
                .setResourceChange(new HashMap<>());
        schedule.special.dummyChronoProperty = new ChronoProperty()
                .setChangeable(1)
                .setMovable(1)
                .setSplittable(1)
                .setGravity(0)
                .setStartTime(null)
                .setDeadline(schedule.getProblemTimelineBlock().getBlockEndTime());
        schedule.special.dummyTimelineProperty = new TimelineProperty()
                .setDeleted(0)
                .setRownum(null)
                .setTimelineid(null)
                .setDependencyIdList(new HashSet<>());

        // Set Planning Facts
        schedule.special.dummyExecutionMode = new ExecutionMode()
                .setVolatileFlag(true)
                .setSchedule(schedule)
                .setTitle("dummyExecutionMode")
                .setDescription("")
                .setExecutionModeIndex(0)
                .setHumanStateChange(schedule.special.dummyHumamStateChange)
                .setProgressChange(schedule.special.dummyProgressChange)
                .setResourceStateChange(schedule.special.dummyResourceStateChange)
                .setChronoProperty(schedule.special.dummyChronoProperty)
                .setTimelineProperty(schedule.special.dummyTimelineProperty);
        schedule.special.sourceAllocation = new Allocation()
                .setVolatileFlag(true)
                .setSchedule(schedule)
                .setAllocationTypeSet(Sets.newHashSet(AllocationType.Locked, AllocationType.SOURCE));
        schedule.special.sourceAllocation.setExecutionMode(new ExecutionMode(schedule.special.dummyExecutionMode)
                .setTitle("source").setProgressChange(new ProgressChange().setProgressDelta(1))
                .setChronoProperty(new ChronoProperty()
                        .setChangeable(0).setMovable(0).setSplittable(0).setGravity(0)
                        .setStartTime(schedule.getProblemTimelineBlock().getBlockStartTime()).setDeadline(schedule.getProblemTimelineBlock().getBlockEndTime())));
        schedule.special.sinkAllocation = new Allocation()
                .setVolatileFlag(true)
                .setSchedule(schedule)
                .setAllocationTypeSet(Sets.newHashSet(AllocationType.Locked, AllocationType.SOURCE));
        schedule.special.sinkAllocation.setExecutionMode(new ExecutionMode(schedule.special.dummyExecutionMode)
                .setTitle("sink").setProgressChange(new ProgressChange().setProgressDelta(1)).setChronoProperty(new ChronoProperty()
                        .setChangeable(0).setMovable(0).setSplittable(0).setGravity(0)
                        .setStartTime(schedule.getProblemTimelineBlock().getBlockEndTime()).setDeadline(schedule.getProblemTimelineBlock().getBlockEndTime())));

        // Initialize Lists with dummy facts
        schedule.setAllocationList(new ArrayList<>(
                Arrays.asList(schedule.special.sourceAllocation, schedule.special.sinkAllocation)));
        schedule.setExecutionModeList(new ArrayList<>(
                Arrays.asList(schedule.special.dummyExecutionMode)));

        // Add ExecutionModes from valueEntryMap
        // Add standard jobs
        schedule.getValueEntryMap().entrySet().stream()
                .filter(x -> Arrays.asList("工作", "存取權").contains(x.getValue().getType()))
                .forEach(y -> {
                    for (int i = 0; i < y.getValue().getResourceStateChangeList().size(); i++) {
                        schedule.getExecutionModeList().add(
                                new ExecutionMode()
                                        .setSchedule(schedule)
                                        .setExecutionModeIndex(i)
                                        .setExecutionModeTypes(
                                                Sets.newHashSet(ExecutionModeType.NEW, ExecutionModeType.USABLE))
                                        .setTitle(y.getKey())
                                        .setDescription("")
                                        .setProgressChange(y.getValue().getProgressChangeList().get(i))
                                        .setHumanStateChange(y.getValue().getHumanStateChangeList().get(i))
                                        .setResourceStateChange(y.getValue().getResourceStateChangeList().get(i))
                                        .setChronoProperty(y.getValue().getChronoProperty())
                                        .setTimelineProperty(new TimelineProperty()
                                                .setDeleted(0)
                                                .setRownum(null)
                                                .setTimelineid(null)
                                                .setDependencyIdList(new HashSet<>())));
                    }
                });

        // Add timeline jobs
        schedule.getProblemTimelineBlock().getTimelineEntryList().stream()
                .filter(x -> x.getTimelineProperty().getDeleted() != 1)
                .forEach(y -> {
                    assert y.getTimelineProperty().getTimelineid() != null;
                    assert y.getTimelineProperty().getRownum() != null;
                    // timeline job resource
                    ResourceElement jobResourceElement = new ResourceElement()
                            .setVolatileFlag(true)
                            .setAmt(100)
                            .setLocation(schedule.special.dummyLocation)
                            .setPriorityTimelineIdList(new TreeSet<>());
                    y.getResourceStateChange().addResourceElementToChange(y.getTimelineProperty().getTimelineid().toString(),
                            jobResourceElement);

                    schedule.getValueEntryMap().put(y.getTimelineProperty().getTimelineid().toString(),
                            new ValueEntry().setVolatileFlag(true).setCapacity(100d).setClassification("task"));

                    // add real job
                    ExecutionMode executionMode = new ExecutionMode()
                            .setSchedule(schedule)
                            .setExecutionModeIndex(y.getExecutionMode())
                            .setExecutionModeTypes(
                                    Sets.newHashSet(ExecutionModeType.OLD, ExecutionModeType.UNUSABLE))
                            .setTitle(y.getTitle())
                            .setDescription(y.getDescription())
                            .setProgressChange(y.getProgressChange())
                            .setHumanStateChange(y.getHumanStateChange())
                            .setResourceStateChange(y.getResourceStateChange())
                            .setChronoProperty(y.getChronoProperty())
                            .setTimelineProperty(y.getTimelineProperty());

                    Allocation allocation = new Allocation()
                            .setSchedule(schedule)
                            .setAllocationTypeSet(Sets.newHashSet(
                                    (y.getTimelineProperty().getRownum() >=
                                            schedule.getProblemTimelineBlock().getBlockScheduleAfter()) ?
                                            AllocationType.Unlocked : AllocationType.Locked,
                                    AllocationType.NORMAL));
                    allocation.setExecutionMode(executionMode);
                    schedule.getExecutionModeList().add(executionMode);
                    schedule.getAllocationList().add(schedule.getAllocationList().size() - 1, allocation);

                    // add dummy jobs between
                    for (int k = 0; k < 20; k++) {
                        Allocation allocation1 = new Allocation()
                                .setVolatileFlag(true)
                                .setSchedule(schedule)
                                .setAllocationTypeSet(allocation.getAllocationTypeSet());
                        allocation1.setExecutionMode(schedule.special.dummyExecutionMode);
                        schedule.getAllocationList().add(
                                schedule.getAllocationList().size() - 1,
                                allocation1
                        );
                    }

                    // add job clone
                    schedule.getExecutionModeList().add(
                            new ExecutionMode()
                                    .setSchedule(schedule)
                                    .setExecutionModeIndex(y.getExecutionMode())
                                    .setExecutionModeTypes(
                                            Sets.newHashSet(ExecutionModeType.NEW, ExecutionModeType.USABLE))
                                    .setTitle(y.getTitle())
                                    .setDescription(y.getDescription())
                                    .setProgressChange(y.getProgressChange())
                                    .setHumanStateChange(y.getHumanStateChange())
                                    .setResourceStateChange(y.getResourceStateChange())
                                    .setChronoProperty(new ChronoProperty(y.getChronoProperty())
                                            .setMovable(1)
                                            .setChangeable(1))
                                    .setTimelineProperty(y.getTimelineProperty()));
                });
    }

    public DataStructureBuilder constructChainProperty() {
        schedule.special.sourceAllocation = schedule.getAllocationList().get(0);
        schedule.special.sinkAllocation = schedule.getAllocationList().get(schedule.getAllocationList().size() - 1);
        for (int i = 0; i < schedule.getAllocationList().size(); i++) schedule.getAllocationList().get(i).setIndex(i);

        // Set Scheduled Job requirement
        schedule.special.sinkAllocation.getExecutionMode().getResourceStateChange().setResourceChange(new HashMap<>());
        schedule.getAllocationList().stream()
                .filter(
                        x -> x.getExecutionMode().getExecutionModeTypes().contains(ExecutionModeType.OLD) && isNotChangeable(x))
                .forEach(x -> {
                    schedule.special.sinkAllocation.getExecutionMode().getResourceStateChange().getResourceChange()
                            .put(x.getExecutionMode().getTimelineProperty().getTimelineid().toString(),
                                    new ArrayList<>(Arrays.asList(
                                            new ResourceElement()
                                                    .setVolatileFlag(true)
                                                    .setAmt(-100)
                                                    .setLocation(schedule.special.dummyLocation)
                                                    .setPriorityTimelineIdList(new TreeSet<>()))));
                });

        // Set ProgressDelta
        schedule.getAllocationList().forEach(x -> x.setProgressdelta((int) (x.getExecutionMode().getProgressChange().getProgressDelta() * 100)));

        // Set Delay
        schedule.getAllocationList().forEach(x -> x.setDelay(0));

        // Set PreviousStandstill
        schedule.getAllocationList().forEach(x -> x.setPreviousStandstill(null));
        Allocation prevAllocation = null;
        for (Allocation thisAllocation : schedule.getFocusedAllocationList()) {
            updateAllocationPreviousStandstill(thisAllocation, prevAllocation);
            prevAllocation = thisAllocation;
        }

        // Set PlannedDuration
        schedule.getAllocationList().forEach(x -> x.setPlannedDuration(null));
        schedule.getFocusedAllocationList().forEach(ListenerTools::updatePlanningDuration);

        // Set PredecessorsDoneDate
        schedule.getAllocationList().forEach(x -> x.setPredecessorsDoneDate(null));
        prevAllocation = null;
        for (Allocation thisAllocation : schedule.getFocusedAllocationList()) {
            updatePredecessorsDoneDate(thisAllocation, prevAllocation);
            if (thisAllocation.getExecutionMode().getChronoProperty().getZonedStartTime() != null)
                thisAllocation.setDelay(Math.max(0,
                        (int) Duration.between(
                                thisAllocation.getPredecessorsDoneDate(),
                                thisAllocation.getExecutionMode().getChronoProperty().getZonedStartTime())
                                .toMinutes()));
            prevAllocation = thisAllocation;
        }

        // set ResourceElementMap
        schedule.getAllocationList().forEach(x -> x.setResourceElementMap(null));
        List<Allocation> focusedAllocationList = schedule.getFocusedAllocationList();
        var newResourceElementMap = updateAllocationResourceStateChange(focusedAllocationList, null);
        for (int i = 0; i < focusedAllocationList.size(); i++)
            focusedAllocationList.get(i).setResourceElementMap(newResourceElementMap.get(i));

        return this;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public DataStructureBuilder setSchedule(Schedule schedule) {
        this.schedule = schedule;
        return this;
    }
}
