/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("PjsJob")
public class Job extends AbstractPersistable {
    private String name;
    private String description = "";
    private Project project;
    private JobType jobType;
    private Integer rownum; //negative if new, positive if exist, 0 if to delete
    private Integer timelineid;
    private List<ExecutionMode> executionModeList;
    private List<Job> successorJobList;
    private Integer startDate = null;
    private Integer deadline = null;
    private Integer gravity;
    private Integer splittable;
    private Integer movable;
    private Integer changeable;
    private List<Integer> dependencyTimelineIdList;

    public Job() {
    }

    public Job(String name, JobType jobType) {
        this.name = name;
        this.jobType = jobType;
    }

    public Job(String name, JobType jobType,
               List<Job> jobList,
               Project project) {
        //Set Basic Information
        this.setName(name);
        this.setId(jobList.size());
        this.setProject(project);
        this.setJobType(jobType);
        this.setRownum(null);
        this.setTimelineid(null);
        this.setGravity(0);
        this.setSplittable(1);
        this.setMovable(1);
        this.setChangeable(1);

        //Initialize
        this.setSuccessorJobList(new ArrayList<>());
        this.executionModeList = new ArrayList<>();
        //Update List
        jobList.add(this);
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public Job setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Job setDescription(String description) {
        this.description = description;
        return this;
    }

    public Project getProject() {
        return project;
    }

    public Job setProject(Project project) {
        this.project = project;
        return this;
    }

    public JobType getJobType() {
        return jobType;
    }

    public Job setJobType(JobType jobType) {
        this.jobType = jobType;
        return this;
    }

    public Integer getRownum() {
        return rownum;
    }

    public Job setRownum(Integer rownum) {
        this.rownum = rownum;
        return this;
    }

    public Integer getTimelineid() {
        return timelineid;
    }

    public Job setTimelineid(Integer timelineid) {
        this.timelineid = timelineid;
        return this;
    }

    public List<ExecutionMode> getExecutionModeList() {
        return executionModeList;
    }

    public Job setExecutionModeList(List<ExecutionMode> executionModeList) {
        this.executionModeList = executionModeList;
        return this;
    }

    public List<Job> getSuccessorJobList() {
        return successorJobList;
    }

    public Job setSuccessorJobList(List<Job> successorJobList) {
        this.successorJobList = successorJobList;
        return this;
    }

    public Integer getDeadline() {
        return deadline;
    }

    public Job setDeadline(Integer deadline) {
        this.deadline = deadline;
        return this;
    }

    public List<Integer> getDependencyTimelineIdList() {
        return dependencyTimelineIdList;
    }

    public Job setDependencyTimelineIdList(List<Integer> dependencyTimelineIdList) {
        this.dependencyTimelineIdList = dependencyTimelineIdList;
        return this;
    }

    public Integer getGravity() {
        return gravity;
    }

    public Job setGravity(Integer gravity) {
        this.gravity = gravity;
        return this;
    }

    public Integer getSplittable() {
        return splittable;
    }

    public Job setSplittable(Integer splittable) {
        this.splittable = splittable;
        return this;
    }

    public Integer getMovable() {
        return movable;
    }

    public Job setMovable(Integer movable) {
        this.movable = movable;
        return this;
    }

    public Integer getChangeable() {
        return changeable;
    }

    public Job setChangeable(Integer changeable) {
        this.changeable = changeable;
        return this;
    }

    public Integer getStartDate() {
        return startDate;
    }

    public Job setStartDate(Integer startDate) {
        this.startDate = startDate;
        return this;
    }
}
