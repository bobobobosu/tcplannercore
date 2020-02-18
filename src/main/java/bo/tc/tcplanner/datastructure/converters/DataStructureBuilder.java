package bo.tc.tcplanner.datastructure.converters;

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.datastructure.*;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import bo.tc.tcplanner.domain.solver.listeners.ListenerTools;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static bo.tc.tcplanner.app.DroolsTools.getConstrintedTimeRange;
import static bo.tc.tcplanner.domain.solver.listeners.ListenerTools.*;
import static com.google.common.base.Preconditions.checkArgument;

public class DataStructureBuilder {
    Schedule schedule;
    List<Allocation> fullAllocationList;

    public DataStructureBuilder(ValueEntryMap valueEntryMap, TimelineBlock timelineBlock, TimeHierarchyMap timeHierarchyMap) {
        checkArgument(valueEntryMap.checkValid());
        checkArgument(timelineBlock.checkValid());
        checkArgument(timeHierarchyMap.checkValid());

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
        schedule.special.dummyLocation = PropertyConstants.dummyLocation;
        schedule.special.dummyTime = PropertyConstants.dummyTime;
        schedule.special.dummyHumamStateChange = new HumanStateChange()
                .setDuration(0)
                .setCurrentLocation(schedule.special.dummyLocation)
                .setMovetoLocation(schedule.special.dummyLocation)
                .setRequirementTimerange(schedule.special.dummyTime);
        schedule.special.dummyProgressChange = new ProgressChange()
                .setProgressDelta(0.5)
                .setProgressLog(new ArrayList<>())
                .setProgressPreset(new ArrayList<>());
        schedule.special.dummyResourceStateChange = new ResourceStateChange()
                .setResourceChange(new ResourceElementMap())
                .setMode(PropertyConstants.ResourceStateChangeTypes.types.delta.name());
        schedule.special.dummyChronoProperty = new ChronoProperty()
                .setChangeable(1)
                .setMovable(1)
                .setSplittable(1)
                .setGravity(0)
                .setStartTime(schedule.getProblemTimelineBlock().getBlockStartTime())
                .setDeadline(schedule.getProblemTimelineBlock().getBlockEndTime())
                .setDeadline(schedule.getProblemTimelineBlock().getBlockEndTime());
        schedule.special.dummyTimelineProperty = new TimelineProperty()
                .setRownum(0)
                .setTimelineid(null)
                .setDependencyIdList(new ArrayList<>())
                .setTaskChainIdList(new ArrayList<>())
                .setPlanningWindowType(PropertyConstants.PlanningWindowTypes.types.Draft.name());

        // Set Planning Facts
        TimelineEntry dummyTimelineEntry = new TimelineEntry()
                .setVolatileFlag(true)
                .setTitle("\"\"")
                .setDescription("")
                .setExecutionMode(0)
                .setHumanStateChange(schedule.special.dummyHumamStateChange)
                .setProgressChange(schedule.special.dummyProgressChange)
                .setResourceStateChange(schedule.special.dummyResourceStateChange)
                .setChronoProperty(schedule.special.dummyChronoProperty)
                .setTimelineProperty(schedule.special.dummyTimelineProperty);
        Allocation sourceAllocation = new Allocation()
                .setVolatileFlag(true)
                .setSchedule(schedule);
        sourceAllocation.setTimelineEntry(new TimelineEntry()
                .setVolatileFlag(true)
                .setTitle("source")
                .setDescription("")
                .setExecutionMode(0)
                .setHumanStateChange(schedule.special.dummyHumamStateChange)
                .setProgressChange(new ProgressChange().setProgressDelta(1))
                .setResourceStateChange(new ResourceStateChange(schedule.special.dummyResourceStateChange))
                .setChronoProperty(new ChronoProperty()
                        .setChangeable(0).setMovable(0).setSplittable(0).setGravity(0)
                        .setDeadline(schedule.getProblemTimelineBlock().getBlockEndTime()))
                .setTimelineProperty(new TimelineProperty(schedule.special.dummyTimelineProperty)
                        .setPlanningWindowType(PropertyConstants.PlanningWindowTypes.types.History.name())));
        Allocation sinkAllocation = new Allocation()
                .setVolatileFlag(true)
                .setSchedule(schedule);
        sinkAllocation.setTimelineEntry(new TimelineEntry()
                .setVolatileFlag(true)
                .setTitle("sink")
                .setDescription("")
                .setExecutionMode(0)
                .setHumanStateChange(schedule.special.dummyHumamStateChange)
                .setProgressChange(new ProgressChange().setProgressDelta(1))
                .setResourceStateChange(new ResourceStateChange(schedule.special.dummyResourceStateChange))
                .setChronoProperty(new ChronoProperty()
                        .setChangeable(0).setMovable(0).setSplittable(0).setGravity(0)
                        .setDeadline(schedule.getProblemTimelineBlock().getBlockEndTime()))
                .setTimelineProperty(new TimelineProperty(schedule.special.dummyTimelineProperty)
                        .setRownum(Integer.MAX_VALUE)
                        .setPlanningWindowType(PropertyConstants.PlanningWindowTypes.types.History.name())));

        // Initialize Lists with dummy facts
        // First, last allocations must follow this order
        schedule.setAllocationList(new ArrayList<>(Arrays.asList(sourceAllocation, sinkAllocation)));
        // First, second and third timelineEntry must follow this order
        schedule.setTimelineEntryList(new ArrayList<TimelineEntry>(
                Arrays.asList(dummyTimelineEntry,
                        sourceAllocation.getTimelineEntry(),
                        sinkAllocation.getTimelineEntry())));
        schedule.setJob2jobcloneMap(new IdentityHashMap<>());

        // Add TimelineEntries from valueEntryMap
        // Add jobs from ValueEntryMap
        schedule.getValueEntryMap().entrySet().stream()
                .filter(x -> Arrays.asList("工作", "存取權").contains(x.getValue().getType()))
                .forEach(y -> {
                    for (int i = 0; i < y.getValue().getResourceStateChangeList().size(); i++) {
                        schedule.getTimelineEntryList().add(
                                new TimelineEntry()
                                        .setExecutionMode(i)
                                        .setTitle(y.getKey())
                                        .setDescription("")
                                        .setProgressChange(y.getValue().getProgressChangeList().get(i)
                                                .setProgressLog(new ArrayList<>()))
                                        .setHumanStateChange(y.getValue().getHumanStateChangeList().get(i))
                                        .setResourceStateChange(y.getValue().getResourceStateChangeList().get(i))
                                        .setChronoProperty(new ChronoProperty(y.getValue().getChronoProperty())
                                                .setStartTime(schedule.getProblemTimelineBlock().getBlockStartTime())
                                                .setDeadline(schedule.getProblemTimelineBlock().getBlockEndTime()))
                                        .setTimelineProperty(new TimelineProperty()
                                                .setRownum(0)
                                                .setTimelineid(null)
                                                .setDependencyIdList(new ArrayList<>())
                                                .setTaskChainIdList(new ArrayList<>())
                                                .setPlanningWindowType(PropertyConstants.PlanningWindowTypes.types.Draft.name())));
                    }
                });

        // add jobs from ProblemTimelineBlock
        schedule.getProblemTimelineBlock().getTimelineEntryList().stream()
                .filter(x -> !x.getTimelineProperty().getPlanningWindowType()
                        .equals(PropertyConstants.PlanningWindowTypes.types.Deleted.name()))
                .forEach(y -> {
                    // timeline job resource
                    ResourceElement jobResourceElement = new ResourceElement()
                            .setVolatileFlag(true)
                            .setAmt(100)
                            .setLocation(schedule.special.dummyLocation)
                            .setPriorityTimelineIdList(new ArrayList<>());
                    schedule.getValueEntryMap().put(y.getTimelineProperty().getTimelineid().toString(),
                            new ValueEntry().setVolatileFlag(true).setCapacity(100d).setClassification("task"));


                    // add real job
                    TimelineEntry timelineEntry;
                    timelineEntry = new TimelineEntry(y);
                    timelineEntry.getResourceStateChange().addResourceElementToChange(y.getTimelineProperty().getTimelineid().toString(),
                            jobResourceElement);
                    Allocation allocation = new Allocation().setSchedule(schedule);
                    allocation.setTimelineEntry(timelineEntry);
                    schedule.getTimelineEntryList().add(timelineEntry);
                    schedule.getAllocationList().add(schedule.getAllocationList().size() - 1, allocation);

                    // add job clone
                    if (y.getChronoProperty().getSplittable() == 1) {
                        TimelineEntry timelineEntryClone;
                        timelineEntryClone = new TimelineEntry(timelineEntry)
                                .setChronoProperty(new ChronoProperty(y.getChronoProperty())
                                        .setMovable(1)
                                        .setChangeable(1))
                                .setTimelineProperty(new TimelineProperty(y.getTimelineProperty())
                                        .setTimelineid(null)
                                        .setPlanningWindowType(PropertyConstants.PlanningWindowTypes.types.Draft.name()));
                        schedule.getTimelineEntryList().add(timelineEntryClone);

                        // Update Map
                        schedule.getJob2jobcloneMap().put(timelineEntry, timelineEntryClone);
                    }


                });

        // add dummy jobs
        for (int i = schedule.getAllocationList().size() - 1; i >= 0; i--) {
            if (schedule.getAllocationList().get(i).getTimelineEntry().getChronoProperty().getStartTime() != null &&
                    schedule.getAllocationList().get(i).getTimelineEntry().getChronoProperty().getZonedStartTime().isBefore(
                            schedule.getProblemTimelineBlock().getZonedBlockScheduleAfter())) break;

            // add dummy jobs between
            int finalI = i;
            IntStream.rangeClosed(1, 30).forEach(x -> {
                Allocation allocation = new Allocation()
                        .setVolatileFlag(true)
                        .setSchedule(schedule);
                allocation.setTimelineEntry(dummyTimelineEntry);
                schedule.getAllocationList().add(finalI, allocation);
            });


        }
        schedule.getAllocationList().forEach(x -> x.setScored(true));
        schedule.getAllocationList().forEach(x -> x.setPinned(false));
        fullAllocationList = new ArrayList<>(schedule.getAllocationList());
    }

    public DataStructureBuilder constructChainProperty() {
        return constructChainProperty(schedule);
    }

    public DataStructureBuilder constructChainProperty(Schedule schedule) {
        Allocation sourceAllocation = schedule.getSourceAllocation();
        Allocation sinkAllocation = schedule.getSinkAllocation();

        for (int i = 0; i < schedule.getAllocationList().size(); i++) {
            schedule.getAllocationList().get(i).setIndex(i);
            schedule.getAllocationList().get(i).setSchedule(schedule);
        }
//        schedule.getAllocationList().forEach(x -> x.getTimelineEntry().getAllocatedList().add(x));
        List<Allocation> focusedAllocationList = schedule.getAllocationList()
                .stream().filter(Allocation::isFocused).collect(Collectors.toList());

        TreeSet<Allocation> focusedAllocationSet = new TreeSet<>(Comparator.comparing(Allocation::getIndex));
        focusedAllocationSet.addAll(focusedAllocationList);
        schedule.focusedAllocationSet = focusedAllocationSet;

        // Set Scheduled Job requirement
        schedule.getSinkAllocation().getTimelineEntry().getResourceStateChange().setResourceChange(new ResourceElementMap());
        schedule.getAllocationList().stream()
                .filter(
                        x -> x.getTimelineEntry().getTimelineProperty().getPlanningWindowType()
                                .equals(PropertyConstants.PlanningWindowTypes.types.Published.name()) &&
                                x.getTimelineEntry().getChronoProperty().getChangeable() == 0)
                .forEach(x -> {
                    schedule.getSinkAllocation().getTimelineEntry().getResourceStateChange().getResourceChange()
                            .put(x.getTimelineEntry().getTimelineProperty().getTimelineid().toString(),
                                    new ArrayList<>(Arrays.asList(
                                            new ResourceElement()
                                                    .setVolatileFlag(true)
                                                    .setAmt(-100)
                                                    .setLocation(schedule.special.dummyLocation)
                                                    .setPriorityTimelineIdList(new ArrayList<>()))));
                });

        // Set ProgressDelta
        schedule.getAllocationList().forEach(x -> x.setProgressdelta((int) Math.round(x.getTimelineEntry().getProgressChange().getProgressDelta() * 100)));

        // Set Delay
        schedule.getAllocationList().forEach(x -> x.setDelay(0));

        // Set PreviousStandstill
        for (int i = 1; i < schedule.getAllocationList().size(); i++)
            schedule.getAllocationList().get(i).setPreviousStandstill(null);
        if (schedule.getSourceAllocation().getPreviousStandstill() == null)
            schedule.getSourceAllocation().setPreviousStandstill(schedule.special.dummyLocation);

        for (int prevIdx = 0, thidIdx = 1; thidIdx < focusedAllocationList.size(); prevIdx++, thidIdx++) {
            updateAllocationPreviousStandstill(focusedAllocationList.get(thidIdx), focusedAllocationList.get(prevIdx));
        }

        // Set PlannedDuration
        schedule.getAllocationList().forEach(x -> x.setPlannedDuration(null));
        focusedAllocationList.forEach(ListenerTools::updatePlanningDuration);

        // Set PredecessorsDoneDate
        for (int i = 1; i < schedule.getAllocationList().size(); i++)
            schedule.getAllocationList().get(i).setPredecessorsDoneDate(null);
        if (schedule.getSourceAllocation().getPredecessorsDoneDate() == null)
            schedule.getSourceAllocation().setPredecessorsDoneDate(schedule.getProblemTimelineBlock().getZonedBlockStartTime());
        for (int prevIdx = 0, thidIdx = 1; thidIdx < focusedAllocationList.size(); prevIdx++, thidIdx++) {
            Allocation prevAllocation = focusedAllocationList.get(prevIdx);
            Allocation thisAllocation = focusedAllocationList.get(thidIdx);
            updatePredecessorsDoneDate(thisAllocation, prevAllocation);
            if (thisAllocation.getTimelineEntry().getChronoProperty().getZonedStartTime() != null)
                thisAllocation.setDelay(Math.max(0,
                        (int) Duration.between(
                                thisAllocation.getPredecessorsDoneDate(),
                                thisAllocation.getTimelineEntry().getChronoProperty().getZonedStartTime())
                                .toMinutes()));
        }


        // set ResourceElementMap
        for (int i = 0; i < schedule.getAllocationList().size(); i++)
            schedule.getAllocationList().get(i).setResourceElementMap(null);
        var newResourceElementMap = updateAllocationResourceStateChange(
                focusedAllocationList,
                schedule.getAllocationList().stream().flatMap(x ->
                        x.getTimelineEntry().getResourceStateChange().getResourceChange().keySet().stream())
                        .collect(Collectors.toSet()));
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

    public List<Allocation> getFullAllocationList() {
        return fullAllocationList;
    }
}
