/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bo.tc.tcplanner.domain;

import bo.tc.tcplanner.datastructure.TimeEntryMap;
import bo.tc.tcplanner.datastructure.TimelineBlock;
import bo.tc.tcplanner.datastructure.ValueEntryMap;
import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactProperty;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.persistence.xstream.api.score.buildin.bendable.BendableScoreXStreamConverter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@PlanningSolution
@XStreamAlias("PjsSchedule")
public class Schedule extends AbstractPersistable {

    //Domain
    private List<Project> projectList;
    private List<Job> jobList;
    private List<ExecutionMode> executionModeList;
    private ValueEntryMap valueEntryMap;
    private TimeEntryMap timeEntryMap;
    private List<Allocation> allocationList;
    private ZonedDateTime globalStartTime;
    private ZonedDateTime globalEndTime;
    private int globalStartRow;
    private int globalEndRow;
    private int globalScheduleAfterIndex;

    public TimelineBlock getProblemTimelineBlock() {
        return problemTimelineBlock;
    }

    public void setProblemTimelineBlock(TimelineBlock problemTimelineBlock) {
        this.problemTimelineBlock = problemTimelineBlock;
    }

    private TimelineBlock problemTimelineBlock;

    @Override
    public Schedule removeVolatile() {
        projectList.removeIf(AbstractPersistable::isVolatileFlag);
        projectList.forEach(Project::removeVolatile);
        jobList.removeIf(AbstractPersistable::isVolatileFlag);
        jobList.forEach(Job::removeVolatile);
        executionModeList.removeIf(AbstractPersistable::isVolatileFlag);
        executionModeList.forEach(ExecutionMode::removeVolatile);
        valueEntryMap.forEach((k, v) -> v.removeVolatile());
        valueEntryMap.entrySet().removeIf(x -> x.getValue().isVolatileFlag());
        allocationList.removeIf(AbstractPersistable::isVolatileFlag);
        allocationList.forEach(Allocation::removeVolatile);
        return this;
    }

    @XStreamConverter(BendableScoreXStreamConverter.class)
    private BendableScore score;


    public Schedule() {
        //Initialize
        projectList = new ArrayList<>();
        jobList = new ArrayList<>();
        executionModeList = new ArrayList<>();
        allocationList = new ArrayList<>();
    }


    public Schedule(Schedule other) {
        this.projectList = new ArrayList<>(other.projectList);
        this.jobList = new ArrayList<>(other.jobList);
        this.executionModeList = new ArrayList<>(other.executionModeList);
        this.allocationList = new ArrayList<>(other.allocationList);
        this.globalStartTime = other.globalStartTime;
        this.globalEndTime = other.globalEndTime;
        this.score = null;
    }

    @ProblemFactProperty
    public ValueEntryMap getValueEntryMap() {
        return valueEntryMap;
    }

    public Schedule setValueEntryMap(ValueEntryMap valueEntryMap) {
        this.valueEntryMap = valueEntryMap;
        return this;
    }

    @ProblemFactProperty
    public TimeEntryMap getTimeEntryMap() {
        return timeEntryMap;
    }

    public void setTimeEntryMap(TimeEntryMap timeEntryMap) {
        this.timeEntryMap = timeEntryMap;
    }

    @ProblemFactCollectionProperty
    public List<Project> getProjectList() {
        return projectList;
    }

    public Schedule setProjectList(List<Project> projectList) {
        this.projectList = projectList;
        return this;
    }

    @ProblemFactCollectionProperty
    public List<Job> getJobList() {
        return jobList;
    }

    public Schedule setJobList(List<Job> jobList) {
        this.jobList = jobList;
        return this;
    }

    @ProblemFactCollectionProperty
    public List<ExecutionMode> getExecutionModeList() {
        return executionModeList;
    }

    public Schedule setExecutionModeList(List<ExecutionMode> executionModeList) {
        this.executionModeList = executionModeList;
        return this;
    }


    @PlanningEntityCollectionProperty
    public List<Allocation> getAllocationList() {
        return allocationList;
    }

    public Schedule setAllocationList(List<Allocation> allocationList) {
        this.allocationList = allocationList;
        return this;
    }

    @PlanningScore(bendableHardLevelsSize = 5, bendableSoftLevelsSize = 4)
    public BendableScore getScore() {
        return score;
    }

    public void setScore(BendableScore score) {
        this.score = score;
    }

    public ZonedDateTime getGlobalStartTime() {
        return globalStartTime;
    }

    public Schedule setGlobalStartTime(ZonedDateTime globalStartTime) {
        this.globalStartTime = globalStartTime;
        return this;
    }

    public ZonedDateTime getGlobalEndTime() {
        return globalEndTime;
    }

    public Schedule setGlobalEndTime(ZonedDateTime globalEndTime) {
        this.globalEndTime = globalEndTime;
        return this;
    }

    public int getGlobalStartRow() {
        return globalStartRow;
    }

    public Schedule setGlobalStartRow(int globalStartRow) {
        this.globalStartRow = globalStartRow;
        return this;
    }

    public int getGlobalEndRow() {
        return globalEndRow;
    }

    public Schedule setGlobalEndRow(int globalEndRow) {
        this.globalEndRow = globalEndRow;
        return this;
    }

    public int getGlobalScheduleAfterIndex() {
        return globalScheduleAfterIndex;
    }

    public Schedule setGlobalScheduleAfterIndex(int globalScheduleAfterIndex) {
        this.globalScheduleAfterIndex = globalScheduleAfterIndex;
        return this;
    }


    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
