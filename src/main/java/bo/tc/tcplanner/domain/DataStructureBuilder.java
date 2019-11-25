package bo.tc.tcplanner.domain;

import bo.tc.tcplanner.datastructure.*;
import org.kie.api.definition.rule.All;

import java.lang.reflect.Array;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bo.tc.tcplanner.app.Toolbox.ZonedDatetime2OffsetMinutes;
import static bo.tc.tcplanner.domain.solver.listeners.ListenerTools.updateAllocationPreviousStandstill;

public class DataStructureBuilder {
    public static Job dummyJob;
    public static Job sourceJob;
    public static Job sinkJob;
    public static ExecutionMode dummyExecutionMode;
    public static ExecutionMode sourceExecutionMode;
    public static ExecutionMode sinkExecutionMode;
    public static HumanStateChange dummyHumamStateChange;
    public static ProgressChange dummyProgressChange;
    public static String dummyLocation;
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
        // dummyrequirementTimerange
        dummyrequirementTimerange = "Anytime";
        // dummy schedule
        defaultSchedule = new Schedule();
        // dummy project
        defaultProject = new Project(defaultSchedule, listOfProjects);
        // dummy humanStateChange
        dummyHumamStateChange = new HumanStateChange(dummyLocation, dummyLocation, 0, "Anytime");
        // dummy dummyProgressChange
        dummyProgressChange = new ProgressChange(100);
        // dummy jobs
        dummyJob = new Job("dummyJob", JobType.STANDARD, listOfJobs, defaultProject);
        sourceJob = new Job("sourceJob", JobType.SOURCE, listOfJobs, defaultProject);
        sourceJob.setMovable(0);
        sourceJob.setSplittable(0);
        sourceJob.setChangeable(0);
        sinkJob = new Job("sinkJob", JobType.SINK, listOfJobs, defaultProject);
        sinkJob.setMovable(0);
        sinkJob.setSplittable(0);
        sinkJob.setChangeable(0);
        // dummy executionModes
        dummyExecutionMode = new ExecutionMode(dummyHumamStateChange, listOfExecutionMode, dummyJob);
        dummyExecutionMode.setProgressChange(dummyProgressChange);

        sourceExecutionMode = new ExecutionMode(dummyHumamStateChange, listOfExecutionMode, sourceJob);
        sourceExecutionMode.setProgressChange(dummyProgressChange);

        sinkExecutionMode = new ExecutionMode(dummyHumamStateChange, listOfExecutionMode, sinkJob);
        sinkExecutionMode.setProgressChange(dummyProgressChange);
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

        // set PredecessorsDoneDate
        for (Allocation allocation : allocationList) {
            int doneDate = 0;
            for (Allocation predecessorAllocation : allocation.getPredecessorAllocationList()) {
                int endDate = predecessorAllocation.getEndDate();
                doneDate = Math.max(doneDate, endDate);
            }
            allocation.setPredecessorsDoneDate(doneDate);
        }

        // set PreviousStandstill
        for (Allocation allocation : allocationList) {
            if (allocation.getPredecessorAllocationList().size() == 0) {
                allocation.setPreviousStandstill(dummyLocation);
            } else {
                updateAllocationPreviousStandstill(allocation);
            }
        }

        // Set Scheduled Job requirement
        Allocation sinkAllocation = allocationList.get(allocationList.size() - 1);
        sinkAllocation.getExecutionMode().getResourceStateChange().setResourceChange(new HashMap<>());
        for (Allocation allocation : allocationList) {
            if (allocation.getJob().getJobType() == JobType.SCHEDULED && allocation.getJob().getChangeable() == 0) {
                if (allocation.getJob().getTimelineid() > 0) {
                    sinkAllocation.getExecutionMode().getResourceStateChange().getResourceChange().put(
                            allocation.getJob().getId().toString(), new ResourceElement(-1, dummyLocation, dummyLocation));
                }
            }
        }
    }

    public void setGlobalProperties(TimelineBlock timelineBlock) {
        // Set source sink time
        defaultSchedule.setGlobalStartTime(ZonedDateTime.parse(timelineBlock.getBlockStartTime()));
        defaultSchedule.setGlobalEndTime(ZonedDateTime.parse(timelineBlock.getBlockEndTime()));
        defaultSchedule.setGlobalStartRow(timelineBlock.getBlockStartRow());
        defaultSchedule.setGlobalEndRow(timelineBlock.getBlockEndRow());
    }

    public void addResourcesFromValueEntryMap(ValueEntryMap valueEntryMap, Project project) {
        this.valueEntryMap = valueEntryMap;
    }

    public void addJobsFromValueEntryDict(ValueEntryMap ValueEntryMap, Project project) {
        this.valueEntryMap = ValueEntryMap;
        //Add standard jobs
        for (Map.Entry<String, ValueEntry> valueEntry : ValueEntryMap.entrySet()) {
            if (valueEntry.getValue().getType().equals("工作")) {
                Job thisjob = new Job(valueEntry.getKey(), JobType.STANDARD, listOfJobs, project);
                thisjob.setSplittable(valueEntry.getValue().getSplittable());
                thisjob.setMovable(valueEntry.getValue().getMovable());
                thisjob.setChangeable(valueEntry.getValue().getChangeable());
                List<ResourceStateChange> resourceStateChangeList = valueEntry.getValue().getResourceStateChangeList();
                List<HumanStateChange> humanStateChangeList = valueEntry.getValue().getHumanStateChangeList();
                List<ProgressChange> progressChangeList = valueEntry.getValue().getProgressChangeList();
                for (int i = 0; i < resourceStateChangeList.size(); i++) {
                    // humanStateChange
                    ExecutionMode thisExecutionMode = new ExecutionMode(humanStateChangeList.get(i), listOfExecutionMode, thisjob);

                    // resourceStateChange
                    thisExecutionMode.setResourceStateChange(resourceStateChangeList.get(i));

                    // progressChange
                    thisExecutionMode.setProgressChange(progressChangeList.get(i));
                }
            }
        }
        int g = 0;
    }

    public void addJobsFromTimelineBlock(TimelineBlock timelineBlock, Project project) {
        this.timelineBlock = timelineBlock;

        for (TimelineEntry timelineEntry : timelineBlock.getTimelineEntryList()) {
            // Add timeline jobs SCHEDULED
            Job mandJob = new Job(timelineEntry.getTitle(), JobType.SCHEDULED, listOfJobs, project);
            mandJob.setDescription(timelineEntry.getDescription());
            mandJob.setDependencyTimelineIdList(timelineEntry.getDependencyIdList());
            mandJob.setRownum(timelineEntry.getRownum());
            mandJob.setTimelineid(timelineEntry.getId());
            mandJob.setDeadline(ZonedDatetime2OffsetMinutes(this.defaultSchedule.getGlobalStartTime(),
                    timelineEntry.getDeadline() != null ? ZonedDateTime.parse(timelineEntry.getDeadline()) : this.defaultSchedule.getGlobalEndTime()));
            mandJob.setGravity(timelineEntry.getGravity());
            mandJob.setSplittable(timelineEntry.getSplittable());
            mandJob.setMovable(timelineEntry.getMovable());
            mandJob.setChangeable(timelineEntry.getChangeable());

            // humanStateChange
            ExecutionMode thisExecutionMode = new ExecutionMode(timelineEntry.getHumanStateChange(), listOfExecutionMode, mandJob);
            thisExecutionMode.setHumanStateChange(timelineEntry.getHumanStateChange());


            // resourceStateChange
            thisExecutionMode.setResourceStateChange(timelineEntry.getResourceStateChange());
            thisExecutionMode.getResourceStateChange().getResourceChange().put(mandJob.getId().toString(), new ResourceElement(1, dummyLocation, dummyLocation));

            // progressChange
            thisExecutionMode.setProgressChange(timelineEntry.getProgressChange());

            // Add timeline jobs STANDARD
            Job stdJob = new Job(timelineEntry.getTitle() , JobType.STANDARD, listOfJobs, project);
            stdJob.setDescription(timelineEntry.getDescription());
            stdJob.setDependencyTimelineIdList(timelineEntry.getDependencyIdList());
            stdJob.setDeadline(ZonedDatetime2OffsetMinutes(this.defaultSchedule.getGlobalStartTime(),
                    timelineEntry.getDeadline() != null ? ZonedDateTime.parse(timelineEntry.getDeadline()) : this.defaultSchedule.getGlobalEndTime()));
            stdJob.setGravity(timelineEntry.getGravity());
            stdJob.setSplittable(timelineEntry.getSplittable());
            stdJob.setMovable(timelineEntry.getMovable());
            stdJob.setChangeable(timelineEntry.getChangeable());

            // humanStateChange
            ExecutionMode stdExecutionMode = new ExecutionMode(timelineEntry.getHumanStateChange(), listOfExecutionMode, stdJob);
            stdExecutionMode.setHumanStateChange(timelineEntry.getHumanStateChange());


            // resourceStateChange
            stdExecutionMode.setResourceStateChange(timelineEntry.getResourceStateChange());
            stdExecutionMode.getResourceStateChange().getResourceChange().put(mandJob.getId().toString(), new ResourceElement(1, dummyLocation, dummyLocation));

            // progressChange
            thisExecutionMode.setProgressChange(timelineEntry.getProgressChange());

            // Add ValueEntry to Map
            ValueEntry timelineValueEntry = new ValueEntry();
            timelineValueEntry.setCapacity(1d);
            timelineValueEntry.setClassification("task");
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
    }

    public Schedule getFullSchedule() {
        defaultSchedule.setAllocationList(listOfAllocations);
        constructChainProperty(defaultSchedule.getAllocationList());
        defaultSchedule.setValueEntryMap(valueEntryMap);
        return defaultSchedule;
    }

    public List<Allocation> getListOfAllocations() {
        return listOfAllocations;
    }
}
