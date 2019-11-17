package bo.tc.tcplanner.domain;

import bo.tc.tcplanner.datastructure.*;

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
        dummyProgressChange = new ProgressChange(0, 1, 1);
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
        //ExecutionModeList
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
            // Create Job
            Job mandJob = new Job(timelineEntry.getTitle(), JobType.MANDATORY, listOfJobs, project);
            mandJob.setDescription(timelineEntry.getDescription());
            mandJob.setDependencyTimelineIdList(timelineEntry.getDependencyIdList());
            mandJob.setRownum(timelineEntry.getRownum());
            mandJob.setTimelineid(timelineEntry.getId());
            mandJob.setDeadline(ZonedDatetime2OffsetMinutes(this.defaultSchedule.getGlobalStartTime(),
                    defaultSchedule.getGlobalEndTime()));
            mandJob.setGravity(timelineEntry.getGravity());
            mandJob.setSplittable(timelineEntry.getSplittable());
            mandJob.setMovable(timelineEntry.getMovable());
            mandJob.setChangeable(timelineEntry.getChangeable());

            // humanStateChange
            ExecutionMode thisExecutionMode = new ExecutionMode(timelineEntry.getHumanStateChange(), listOfExecutionMode, mandJob);
            thisExecutionMode.setHumanStateChange(timelineEntry.getHumanStateChange());

            // resourceStateChange
            thisExecutionMode.setResourceStateChange(timelineEntry.getResourceStateChange());

            // progressChangee
            thisExecutionMode.setProgressChange(timelineEntry.getProgressChange());
        }
        int g = 0;
    }

    public void initializeAllocationList(int dummyLengthInChain) {
        //Add Source
        Allocation sourceallocation = new Allocation(sourceExecutionMode, listOfAllocations, 100);
        sourceallocation.setForceStartTime(0);
        sourceallocation.setForceEndTime(0);
        sourceallocation.setAllocationType(AllocationType.Locked);

        //Add Mandatory Jobs: Fixed Length Chain Mode
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
                mandallocation.setForceEndTime(ZonedDatetime2OffsetMinutes(defaultSchedule.getGlobalStartTime(),
                        ZonedDateTime.parse(timelineEntry.getStartTime()).plusMinutes((long) timelineEntry.getHumanStateChange().getDuration())));
            }

            for(int k=0;k<dummyLengthInChain;k++){
                Allocation allocation = new Allocation(dummyExecutionMode, listOfAllocations, 10);
                allocation.setAllocationType(lockStatus);
            }
//            if (i + 1 < timelineBlock.getTimelineEntryList().size()) {
//                int timeGap = ZonedDatetime2OffsetMinutes(defaultSchedule.getGlobalStartTime(), ZonedDateTime.parse(timelineBlock.getTimelineEntryList().get(i + 1).getStartTime()))
//                        - mandallocation.getEndDate();
//                do {
//                    timeGap -= dummyLengthInChain;
//                    Allocation allocation = new Allocation(dummyExecutionMode, listOfAllocations, 100);
//                    allocation.setAllocationType(lockStatus);
//                } while ((timeGap > dummyLengthInChain));
//            }
        }

        //Add Sink
        Allocation sinkallocation = new Allocation(sinkExecutionMode, listOfAllocations, 100);
        sinkallocation.setForceStartTime(ZonedDatetime2OffsetMinutes(defaultSchedule.getGlobalStartTime(), defaultSchedule.getGlobalEndTime()));
        sinkallocation.setForceEndTime(ZonedDatetime2OffsetMinutes(defaultSchedule.getGlobalStartTime(), defaultSchedule.getGlobalEndTime()));
        sinkallocation.setAllocationType(AllocationType.Locked);


    }

    //// Getter Setter
    public Project getDefaultProject() {
        return defaultProject;
    }

    public ExecutionMode getDummyExecutionMode() {
        return dummyExecutionMode;
    }

    public Schedule getDefaultSchedule() {
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
        return defaultSchedule;
    }

    //// Toolbox
    public List<Allocation> getListOfAllocations() {
        return listOfAllocations;
    }
}
