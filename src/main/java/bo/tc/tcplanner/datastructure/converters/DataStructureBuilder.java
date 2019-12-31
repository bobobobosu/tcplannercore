package bo.tc.tcplanner.datastructure.converters;

import bo.tc.tcplanner.datastructure.*;
import bo.tc.tcplanner.domain.*;
import bo.tc.tcplanner.domain.solver.listeners.NonDummyAllocationIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.RangeSet;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeRangeSet;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static bo.tc.tcplanner.app.DroolsTools.getConstrintedTimeRange;
import static bo.tc.tcplanner.app.TCSchedulingApp.timeEntryMap;
import static bo.tc.tcplanner.app.TCSchedulingApp.timeHierarchyMap;
import static bo.tc.tcplanner.domain.solver.filters.FilterTools.isNotChangeable;
import static bo.tc.tcplanner.domain.solver.listeners.ListenerTools.*;
import static java.lang.Math.round;


public class DataStructureBuilder {
    public static Integer deletedRownum = 99999;
    public static Job dummyJob;
    public static String dummyLocation;
    public static String dummyTime;
    public Job sourceJob;
    public Job sinkJob;
    public ExecutionMode dummyExecutionMode;
    public ExecutionMode sourceExecutionMode;
    public ExecutionMode sinkExecutionMode;
    public HumanStateChange dummyHumamStateChange;
    public ProgressChange dummyProgressChange;
    /*
     * id is set from global list before being added into the list Setter only
     * modify global lists(ex. listOfxxx) & maps(ex. xxToyy), lists in each problem
     * facts will be set in getDefaultSchedule()
     */
    // Planning Facts
    List<Project> listOfProjects;
    List<Job> listOfJobs;
    List<ExecutionMode> listOfExecutionMode;
    ValueEntryMap valueEntryMap;
    // Planning Entity
    List<Allocation> listOfAllocations;
    // dummy variables
    Schedule defaultSchedule;
    Project defaultProject;
    String dummyrequirementTimerange;
    // Save Data
    TimelineBlock timelineBlock;
    // Easy Access
    Map<TimelineEntry, ExecutionMode> timelineEntryExecutionModeMap;

    public ValueEntryMap getValueEntryMap() {
        return valueEntryMap;
    }

    public void setValueEntryMap(ValueEntryMap valueEntryMap) {
        this.valueEntryMap = valueEntryMap;
    }

    public DataStructureBuilder() {

        //// Initialize Planning Facts
        listOfJobs = new ArrayList<>();
        listOfExecutionMode = new ArrayList<>();
        listOfProjects = new ArrayList<>();

        //// Initialize Planning Entity
        listOfAllocations = new ArrayList<>();

        //// Initialize dummy variables
        // dummyLocation
        dummyLocation = "Undefined";
        // dummyTimeRestriction
        dummyTime = "Anytime";
        // dummyrequirementTimerange
        dummyrequirementTimerange = "Anytime";
        // dummy schedule
        defaultSchedule = new Schedule();
        // dummy project
        defaultProject = new Project(defaultSchedule, listOfProjects);
        // dummy humanStateChange
        dummyHumamStateChange = new HumanStateChange().setCurrentLocation(dummyLocation).setMovetoLocation(dummyLocation).setRequirementTimerange(dummyTime).setDuration(0);
        // dummy dummyProgressChange
        dummyProgressChange = new ProgressChange().setProgressDelta(1);
        // dummy jobs
        dummyJob = new Job("dummyJob", JobType.STANDARD, listOfJobs, defaultProject).setTimelineProperty(new TimelineProperty());
        dummyJob.setVolatileFlag(true);
        sourceJob = new Job("sourceJob", JobType.SOURCE, listOfJobs, defaultProject).setTimelineProperty(new TimelineProperty());
        sourceJob.setVolatileFlag(true);
        sinkJob = new Job("sinkJob", JobType.SINK, listOfJobs, defaultProject).setTimelineProperty(new TimelineProperty());
        sinkJob.setVolatileFlag(true);
        // dummy executionModes
        dummyExecutionMode = new ExecutionMode(listOfExecutionMode, dummyJob).setHumanStateChange(dummyHumamStateChange).setProgressChange(dummyProgressChange)
                .setChronoProperty(new ChronoProperty().setChangeable(1).setMovable(1).setSplittable(1));
        dummyExecutionMode.setVolatileFlag(true);
        sourceExecutionMode = new ExecutionMode(listOfExecutionMode, sourceJob).setHumanStateChange(dummyHumamStateChange).setProgressChange(dummyProgressChange)
                .setChronoProperty(new ChronoProperty().setChangeable(0).setMovable(0).setSplittable(0));
        sourceExecutionMode.setVolatileFlag(true);
        sinkExecutionMode = new ExecutionMode(listOfExecutionMode, sinkJob).setHumanStateChange(dummyHumamStateChange).setProgressChange(dummyProgressChange)
                .setChronoProperty(new ChronoProperty().setChangeable(0).setMovable(0).setSplittable(0));
        sinkExecutionMode.setVolatileFlag(true);
    }

    public static void constructChainProperty(List<Allocation> allocationList) {
        // set precedence
        for (int i = 0; i < allocationList.size(); i++) {
            allocationList.get(i).setIndex(i);
            List<Allocation> predecessorList = new ArrayList<>();
            List<Allocation> successorList = new ArrayList<>();
            if (i == 0) {
                successorList.add(allocationList.get(i + 1));
            } else if (i == allocationList.size() - 1) {
                predecessorList.add(allocationList.get(i - 1));
            } else {
                predecessorList.add(allocationList.get(i - 1));
                successorList.add(allocationList.get(i + 1));
            }
            allocationList.get(i).setSourceAllocation(allocationList.get(0));
            allocationList.get(i).setSinkAllocation(allocationList.get(allocationList.size() - 1));
            allocationList.get(i).setSuccessorAllocationList(successorList);
            allocationList.get(i).setPredecessorAllocationList(predecessorList);
        }


        // Set Scheduled Job requirement
        Allocation sourceAllocation = allocationList.get(0);
        Allocation sinkAllocation = allocationList.get(allocationList.size() - 1);
        sinkAllocation.getExecutionMode().getResourceStateChange().setResourceChange(new HashMap<>());
        for (Allocation allocation : allocationList) {
            if (allocation.getJob().getJobType() == JobType.SCHEDULED && isNotChangeable(allocation)) {
                if (allocation.getJob().getTimelineProperty().getRownum() != null) {
                    sinkAllocation.getExecutionMode().getResourceStateChange().getResourceChange().put(
                            allocation.getJob().getId().toString(), new LinkedList<>(Arrays.asList(new ResourceElement(-100, dummyLocation))));
                }
            }
        }


        Allocation prevAllocation;
        Allocation thisAllocation;

        // set PreviousStandstill
        for (Allocation allocation : allocationList) {
            allocation.setPreviousStandstill(null);
        }
        sourceAllocation.setPreviousStandstill(dummyLocation);
        prevAllocation = sourceAllocation;
        while ((thisAllocation = NonDummyAllocationIterator.getNext(prevAllocation)) != null) {
            updateAllocationPreviousStandstill(thisAllocation, prevAllocation);
            prevAllocation = thisAllocation;
        }

        // set PredecessorsDoneDate
        for (Allocation allocation : allocationList) {
            allocation.setPredecessorsDoneDate(null);
            if (allocation.getJob() == dummyJob) {
                allocation.setPlannedDuration(null);
            } else {
                updatePlanningDuration(allocation);
            }
        }

        thisAllocation = sourceAllocation;
        prevAllocation = null;
        do {
            updatePredecessorsDoneDate(thisAllocation, prevAllocation);
            if (thisAllocation.getExecutionMode().getStartDate() != null)
                thisAllocation.setDelay(Math.max(0, (int) Duration.between(thisAllocation.getPredecessorsDoneDate(), thisAllocation.getExecutionMode().getStartDate()).toMinutes()));
            prevAllocation = thisAllocation;
        } while ((thisAllocation = NonDummyAllocationIterator.getNext(prevAllocation)) != null);

        // set ResourceElementMap
        for (Allocation allocation : allocationList) {
            allocation.setResourceElementMap(null);
        }
        List<Allocation> focusedAllocationList = NonDummyAllocationIterator.getAllNextIncludeThis(sourceAllocation);
        var newResourceElementMap = updateAllocationResourceStateChange(focusedAllocationList, null);
        for (int i = 0; i < focusedAllocationList.size(); i++)
            focusedAllocationList.get(i).setResourceElementMap(newResourceElementMap.get(i));
    }

    public void setGlobalProperties(TimelineBlock timelineBlock) {
        // Set source sink time
        defaultSchedule.setGlobalStartTime(ZonedDateTime.parse(timelineBlock.getBlockStartTime()));
        defaultSchedule.setGlobalEndTime(ZonedDateTime.parse(timelineBlock.getBlockEndTime()));
        defaultSchedule.setGlobalStartRow(timelineBlock.getBlockStartRow());
        defaultSchedule.setGlobalEndRow(timelineBlock.getBlockEndRow());
    }

    public void addJobsFromValueEntryDict() {
        //Add standard jobs
        for (Map.Entry<String, ValueEntry> valueEntry : valueEntryMap.entrySet()) {
            if (valueEntry.getValue().getType().equals("工作") || valueEntry.getValue().getType().equals("存取權")) {
                Job thisjob = new Job(valueEntry.getKey(), JobType.STANDARD, listOfJobs, defaultProject)
                        .setTimelineProperty(new TimelineProperty());
                for (int i = 0; i < valueEntry.getValue().getHumanStateChangeList().size(); i++) {
                    // humanStateChange
                    ExecutionMode thisExecutionMode = new ExecutionMode(listOfExecutionMode, thisjob)
                            .setExecutionModeIndex(i)
                            .setHumanStateChange(valueEntry.getValue().getHumanStateChangeList().get(i))
                            .setResourceStateChange(valueEntry.getValue().getResourceStateChangeList().get(i))
                            .setProgressChange(valueEntry.getValue().getProgressChangeList().get(i))
                            .setChronoProperty(valueEntry.getValue().getChronoProperty())
                            .setExecutionModeIndex(i)
                            .setExecutionModeTypes(Sets.newHashSet(ExecutionModeType.NEW, ExecutionModeType.USABLE));
                }
            }
        }
        int g = 0;
    }

    public void addJobsFromTimelineBlock(TimelineBlock timelineBlock) {
        this.timelineBlock = timelineBlock;
        timelineEntryExecutionModeMap = new HashMap<>();

        for (TimelineEntry timelineEntry : timelineBlock.getTimelineEntryList()) {
            if (timelineEntry.getTimelineProperty().getRownum().equals(deletedRownum)) continue;

            // Builtin constraints
            List<ResourceElement> resourceElements = new LinkedList<>(Arrays.asList(
                    new ResourceElement()
                            .setAmt(100)
                            .setLocation(dummyLocation)
                            .setVolatileFlag(true)));


            // Add timeline jobs SCHEDULED
            Job mandJob = new Job(timelineEntry.getTitle(), JobType.SCHEDULED, listOfJobs, defaultProject)
                    .setDescription(timelineEntry.getDescription())
                    .setTimelineProperty(new TimelineProperty(timelineEntry.getTimelineProperty()));

            ExecutionMode thisExecutionMode = new ExecutionMode(listOfExecutionMode, mandJob)
                    .setHumanStateChange(timelineEntry.getHumanStateChange())
                    .setResourceStateChange(timelineEntry.getResourceStateChange())
                    .setProgressChange(timelineEntry.getProgressChange())
                    .setChronoProperty(new ChronoProperty(timelineEntry.getChronoProperty()))
                    .setExecutionModeIndex(0)
                    .setExecutionModeTypes(Sets.newHashSet(ExecutionModeType.OLD, ExecutionModeType.UNUSABLE));

            thisExecutionMode.getResourceStateChange().getResourceChange().put(mandJob.getId().toString(), resourceElements);
            timelineEntryExecutionModeMap.put(timelineEntry, thisExecutionMode);

            ExecutionMode stdExecutionMode = new ExecutionMode(listOfExecutionMode, mandJob)
                    .setHumanStateChange(timelineEntry.getHumanStateChange())
                    .setResourceStateChange(timelineEntry.getResourceStateChange())
                    .setProgressChange(timelineEntry.getProgressChange())
                    .setChronoProperty(new ChronoProperty(timelineEntry.getChronoProperty())
                            .setMovable(1)
                            .setChangeable(1))
                    .setExecutionModeIndex(0)
                    .setExecutionModeTypes(Sets.newHashSet(ExecutionModeType.NEW, ExecutionModeType.USABLE));

            stdExecutionMode.getResourceStateChange().getResourceChange().put(mandJob.getId().toString(), resourceElements);

            // Add ValueEntry to Map
            ValueEntry timelineValueEntry = new ValueEntry().setCapacity(100d).setClassification("task");
            valueEntryMap.put(mandJob.getId().toString(), timelineValueEntry);
        }


        int g = 0;
    }

    public void initializeAllocationList(int dummyLengthInChain) {
        //Add Source
        Allocation sourceallocation = new Allocation(sourceExecutionMode, listOfAllocations, 100);
        sourceallocation.setPredecessorsDoneDate(defaultSchedule.getGlobalStartTime());
        sourceallocation.setAllocationTypeSet(Sets.newHashSet(AllocationType.Locked, AllocationType.SOURCE));

        //Add Scheduled Jobs: Fixed Length Chain Mode
        for (int i = 0; i < timelineBlock.getTimelineEntryList().size(); i++) {
            TimelineEntry timelineEntry = timelineBlock.getTimelineEntryList().get(i);
            AllocationType lockStatus = (timelineEntry.getTimelineProperty().getRownum() >= timelineBlock.getBlockScheduleAfter()) ? AllocationType.Unlocked : AllocationType.Locked;
            Allocation mandallocation = new Allocation(timelineEntryExecutionModeMap.get(timelineEntry), listOfAllocations,
                    Math.toIntExact(round(timelineEntry.getProgressChange().getProgressDelta() * 100)));
            mandallocation.setAllocationTypeSet(Sets.newHashSet(lockStatus, AllocationType.NORMAL));
            if (timelineEntry.getTimelineProperty().getRownum().equals(timelineBlock.getBlockScheduleAfter()))
                defaultSchedule.setGlobalScheduleAfterIndex(mandallocation.getIndex());

            for (int k = 0; k < dummyLengthInChain; k++) {
                Allocation allocation = new Allocation(dummyExecutionMode, listOfAllocations, 10);
                allocation.setAllocationTypeSet(Sets.newHashSet(lockStatus, AllocationType.NORMAL));
            }
        }

        //Add Sink
        Allocation sinkallocation = new Allocation(sinkExecutionMode, listOfAllocations, 100);
        sourceallocation.setAllocationTypeSet(Sets.newHashSet(AllocationType.Locked, AllocationType.SINK));


    }

    //// Getter Setter
    public Project getDefaultProject() {
        return defaultProject;
    }

    public ExecutionMode getDummyExecutionMode() {
        return dummyExecutionMode;
    }

    public void initializeSchedule() {
        for (Project project : listOfProjects) {
            project.getSchedule().getProjectList().add(project);
        }
        for (Job job : listOfJobs) {
            job.getProject().getJobList().add(job);
            job.getProject().getSchedule().getJobList().add(job);
        }
        for (ExecutionMode executionMode : listOfExecutionMode) {
            executionMode.getJob().getExecutionModeList().add(executionMode);
            executionMode.getJob().getProject().getExecutionModeList().add(executionMode);
            executionMode.getJob().getProject().getSchedule().getExecutionModeList().add(executionMode);
        }

        for (Allocation allocation : listOfAllocations) {
            allocation.getProject().getSchedule().getAllocationList().add(allocation);
        }


        defaultSchedule.setTimeEntryMap(new TimeEntryMap());
        timeHierarchyMap.forEach((k, v) -> defaultSchedule.getTimeEntryMap().put(
                k, getConstrintedTimeRange(k,
                        defaultSchedule.getGlobalStartTime(),
                        defaultSchedule.getGlobalEndTime())
        ));
        defaultSchedule.setValueEntryMap(valueEntryMap);
        defaultSchedule.setProblemTimelineBlock(timelineBlock);
    }

    public Schedule getFullSchedule() {
        defaultSchedule.setAllocationList(listOfAllocations);
        constructChainProperty(defaultSchedule.getAllocationList());
        return defaultSchedule;
    }

    public List<Allocation> getListOfAllocations() {
        return listOfAllocations;
    }
}
