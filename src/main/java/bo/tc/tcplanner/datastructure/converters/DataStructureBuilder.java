package bo.tc.tcplanner.datastructure.converters;

import bo.tc.tcplanner.datastructure.*;
import bo.tc.tcplanner.domain.*;
import bo.tc.tcplanner.domain.solver.listeners.NonDummyAllocationIterator;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bo.tc.tcplanner.app.Toolbox.ZonedDatetime2OffsetMinutes;
import static bo.tc.tcplanner.domain.solver.listeners.ListenerTools.*;

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

    public ValueEntryMap getValueEntryMap() {
        return valueEntryMap;
    }

    public void setValueEntryMap(ValueEntryMap valueEntryMap) {
        this.valueEntryMap = valueEntryMap;
    }

    ValueEntryMap valueEntryMap;
    // Planning Entity
    List<Allocation> listOfAllocations;
    // dummy variables
    Schedule defaultSchedule;
    Project defaultProject;
    String dummyrequirementTimerange;
    // Save Data
    TimelineBlock timelineBlock;

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
        dummyProgressChange = new ProgressChange().setProgressDelta(100);
        // dummy jobs
        dummyJob = new Job("dummyJob", JobType.STANDARD, listOfJobs, defaultProject);
        dummyJob.setVolatileFlag(true);
        sourceJob = new Job("sourceJob", JobType.SOURCE, listOfJobs, defaultProject).setMovable(0).setSplittable(0).setChangeable(0);
        sourceJob.setVolatileFlag(true);
        sinkJob = new Job("sinkJob", JobType.SINK, listOfJobs, defaultProject).setMovable(0).setSplittable(0).setChangeable(0);
        sinkJob.setVolatileFlag(true);
        // dummy executionModes
        dummyExecutionMode = new ExecutionMode(listOfExecutionMode, dummyJob).setHumanStateChange(dummyHumamStateChange).setProgressChange(dummyProgressChange);
        dummyExecutionMode.setVolatileFlag(true);
        sourceExecutionMode = new ExecutionMode(listOfExecutionMode, sourceJob).setHumanStateChange(dummyHumamStateChange).setProgressChange(dummyProgressChange);
        sourceExecutionMode.setVolatileFlag(true);
        sinkExecutionMode = new ExecutionMode(listOfExecutionMode, sinkJob).setHumanStateChange(dummyHumamStateChange).setProgressChange(dummyProgressChange);
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
            if (allocation.getJob().getJobType() == JobType.SCHEDULED && allocation.getJob().getChangeable() == 0) {
                if (allocation.getJob().getTimelineid() > 0) {
                    sinkAllocation.getExecutionMode().getResourceStateChange().getResourceChange().put(
                            allocation.getJob().getId().toString(), new ResourceElement(-100, dummyLocation, dummyLocation));
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
        sourceAllocation.setPredecessorsDoneDate(0);
        prevAllocation = sourceAllocation;
        while ((thisAllocation = NonDummyAllocationIterator.getNext(prevAllocation)) != null) {
            updatePredecessorsDoneDate(thisAllocation, prevAllocation);
            prevAllocation = thisAllocation;
        }

        // set ResourceElementMap
        for (Allocation allocation : allocationList) {
            allocation.setResourceElementMap(null);
        }
        sourceAllocation.setResourceElementMap(new HashMap<>());
        prevAllocation = sourceAllocation;
        while ((thisAllocation = NonDummyAllocationIterator.getNext(prevAllocation)) != null) {
            updateAllocationResourceStateChange(thisAllocation, prevAllocation);
            prevAllocation = thisAllocation;
        }

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
            if (valueEntry.getValue().getType().equals("工作")) {
                Job thisjob = new Job(valueEntry.getKey(), JobType.STANDARD, listOfJobs, defaultProject)
                        .setSplittable(valueEntry.getValue().getSplittable())
                        .setMovable(valueEntry.getValue().getMovable())
                        .setChangeable(valueEntry.getValue().getChangeable());
                List<ResourceStateChange> resourceStateChangeList = valueEntry.getValue().getResourceStateChangeList();
                List<HumanStateChange> humanStateChangeList = valueEntry.getValue().getHumanStateChangeList();
                List<ProgressChange> progressChangeList = valueEntry.getValue().getProgressChangeList();
                for (int i = 0; i < resourceStateChangeList.size(); i++) {
                    // humanStateChange
                    ExecutionMode thisExecutionMode = new ExecutionMode(listOfExecutionMode, thisjob)
                            .setHumanStateChange(humanStateChangeList.get(i))
                            .setResourceStateChange(resourceStateChangeList.get(i))
                            .setProgressChange(progressChangeList.get(i));
                }
            }
        }
        int g = 0;
    }

    public void addJobsFromTimelineBlock(TimelineBlock timelineBlock) {
        this.timelineBlock = timelineBlock;

        for (TimelineEntry timelineEntry : timelineBlock.getTimelineEntryList()) {
            if (timelineEntry.getRownum().equals(deletedRownum)) continue;

            // Builtin constraints
            ResourceElement resourceElement = new ResourceElement().setAmt(100).setRequirementLocation(dummyLocation).setProductionLocation(dummyLocation);
            resourceElement.setVolatileFlag(true);

            // Add timeline jobs SCHEDULED
            Job mandJob = new Job(timelineEntry.getTitle(), JobType.SCHEDULED, listOfJobs, defaultProject)
                    .setDescription(timelineEntry.getDescription())
                    .setDependencyTimelineIdList(timelineEntry.getDependencyIdList())
                    .setRownum(timelineEntry.getRownum())
                    .setTimelineid(timelineEntry.getId())
                    .setGravity(timelineEntry.getGravity())
                    .setDeadline(ZonedDatetime2OffsetMinutes(this.defaultSchedule.getGlobalStartTime(),
                            timelineEntry.getDeadline() != null ? ZonedDateTime.parse(timelineEntry.getDeadline()) : this.defaultSchedule.getGlobalEndTime()))
                    .setSplittable(timelineEntry.getSplittable())
                    .setMovable(timelineEntry.getMovable())
                    .setChangeable(timelineEntry.getChangeable());

            ExecutionMode thisExecutionMode = new ExecutionMode(listOfExecutionMode, mandJob)
                    .setHumanStateChange(timelineEntry.getHumanStateChange())
                    .setResourceStateChange(timelineEntry.getResourceStateChange())
                    .setProgressChange(timelineEntry.getProgressChange());

            thisExecutionMode.getResourceStateChange().getResourceChange().put(mandJob.getId().toString(), resourceElement);

            // Add timeline jobs STANDARD
            Job stdJob = new Job(timelineEntry.getTitle(), JobType.STANDARD, listOfJobs, defaultProject)
                    .setDescription(timelineEntry.getDescription())
                    .setDependencyTimelineIdList(timelineEntry.getDependencyIdList())
                    .setGravity(timelineEntry.getGravity())
                    .setDeadline(ZonedDatetime2OffsetMinutes(this.defaultSchedule.getGlobalStartTime(),
                            timelineEntry.getDeadline() != null ? ZonedDateTime.parse(timelineEntry.getDeadline()) : this.defaultSchedule.getGlobalEndTime()))
                    .setSplittable(timelineEntry.getSplittable())
                    .setMovable(timelineEntry.getMovable())
                    .setChangeable(1);

            ExecutionMode stdExecutionMode = new ExecutionMode(listOfExecutionMode, stdJob)
                    .setHumanStateChange(timelineEntry.getHumanStateChange())
                    .setResourceStateChange(timelineEntry.getResourceStateChange())
                    .setProgressChange(timelineEntry.getProgressChange());

            stdExecutionMode.getResourceStateChange().getResourceChange().put(mandJob.getId().toString(), resourceElement);

            // Add ValueEntry to Map
            ValueEntry timelineValueEntry = new ValueEntry().setCapacity(100d).setClassification("task");
            valueEntryMap.put(mandJob.getId().toString(), timelineValueEntry);
        }


        int g = 0;
    }

    public void initializeAllocationList(int dummyLengthInChain) {
        //Add Source
        Allocation sourceallocation = new Allocation(sourceExecutionMode, listOfAllocations, 100);
        sourceallocation.setForceStartTime(0);
        sourceallocation.setAllocationType(AllocationType.Locked);

        //Add Scheduled Jobs: Fixed Length Chain Mode
        HashMap<Integer, ExecutionMode> timelineid2executionModeMap = new HashMap<>();
        for (ExecutionMode executionMode : listOfExecutionMode)
            timelineid2executionModeMap.put(executionMode.getJob().getTimelineid(), executionMode);
        for (int i = 0; i < timelineBlock.getTimelineEntryList().size(); i++) {
            TimelineEntry timelineEntry = timelineBlock.getTimelineEntryList().get(i);
            AllocationType lockStatus = (timelineEntry.getRownum() >= timelineBlock.getBlockScheduleAfter()) ? AllocationType.Unlocked : AllocationType.Locked;
            Allocation mandallocation = new Allocation(timelineid2executionModeMap.get(timelineEntry.getId()), listOfAllocations, 100);
            mandallocation.setAllocationType(lockStatus);
            if (timelineEntry.getRownum().equals(timelineBlock.getBlockScheduleAfter()))
                defaultSchedule.setGlobalScheduleAfterIndex(mandallocation.getIndex());
            if (timelineEntry.getMovable() == 1) {
            } else {
                mandallocation.setForceStartTime(ZonedDatetime2OffsetMinutes(defaultSchedule.getGlobalStartTime(), ZonedDateTime.parse(timelineEntry.getStartTime())));
            }

            for (int k = 0; k < dummyLengthInChain; k++) {
                Allocation allocation = new Allocation(dummyExecutionMode, listOfAllocations, 10);
                allocation.setAllocationType(lockStatus);
            }
        }

        //Add Sink
        Allocation sinkallocation = new Allocation(sinkExecutionMode, listOfAllocations, 100);
        sinkallocation.setForceStartTime(ZonedDatetime2OffsetMinutes(defaultSchedule.getGlobalStartTime(), defaultSchedule.getGlobalEndTime()));
        sinkallocation.setAllocationType(AllocationType.Locked);


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
        defaultSchedule.setValueEntryMap(valueEntryMap);
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
