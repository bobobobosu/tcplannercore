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
    private Integer rownum;
    private Integer timelineid;
    private List<ExecutionMode> executionModeList;
    private List<Job> successorJobList;
    private Integer deadline = null;
    private Integer gravity;
    private Integer splittable;
    private Integer movable;
    private Integer changeable;
    private List<Integer> dependencyTimelineIdList;

    public Job() {
    }

    @Override
    public String toString() {
        return name;
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


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public Integer getRownum() {
        return rownum;
    }

    public void setRownum(Integer rownum) {
        this.rownum = rownum;
    }

    public Integer getTimelineid() {
        return timelineid;
    }

    public void setTimelineid(Integer timelineid) {
        this.timelineid = timelineid;
    }

    public List<ExecutionMode> getExecutionModeList() {
        return executionModeList;
    }

    public void setExecutionModeList(List<ExecutionMode> executionModeList) {
        this.executionModeList = executionModeList;
    }

    public List<Job> getSuccessorJobList() {
        return successorJobList;
    }

    public void setSuccessorJobList(List<Job> successorJobList) {
        this.successorJobList = successorJobList;
    }

    public Integer getDeadline() {
        return deadline;
    }

    public void setDeadline(Integer deadline) {
        this.deadline = deadline;
    }

    public List<Integer> getDependencyTimelineIdList() {
        return dependencyTimelineIdList;
    }

    public void setDependencyTimelineIdList(List<Integer> dependencyTimelineIdList) {
        this.dependencyTimelineIdList = dependencyTimelineIdList;
    }

    public Integer getGravity() {
        return gravity;
    }

    public void setGravity(Integer gravity) {
        this.gravity = gravity;
    }

    public Integer getSplittable() {
        return splittable;
    }

    public void setSplittable(Integer splittable) {
        this.splittable = splittable;
    }

    public Integer getMovable() {
        return movable;
    }

    public void setMovable(Integer movable) {
        this.movable = movable;
    }

    public Integer getChangeable() {
        return changeable;
    }

    public void setChangeable(Integer changeable) {
        this.changeable = changeable;
    }


    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
