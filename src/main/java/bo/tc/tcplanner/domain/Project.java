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

@XStreamAlias("PjsProject")
public class Project extends AbstractPersistable {

    private int releaseDate;
    private int criticalPathDuration;

    private Schedule schedule;
    private List<Job> jobList;
    private List<ExecutionMode> executionModeList;

    public Project() {

    }

    public Project(Schedule schedule, List<Project> projectList) {
        //Set Basic Information
        this.schedule = schedule;
        setId(projectList.size());

        //Initialize
        jobList = new ArrayList<>();
        executionModeList = new ArrayList<>();

        //Update List
        projectList.add(this);
    }

    public int getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(int releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getCriticalPathDuration() {
        return criticalPathDuration;
    }

    public void setCriticalPathDuration(int criticalPathDuration) {
        this.criticalPathDuration = criticalPathDuration;
    }

    public List<Job> getJobList() {
        return jobList;
    }

    public void setJobList(List<Job> jobList) {
        this.jobList = jobList;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public int getCriticalPathEndDate() {
        return releaseDate + criticalPathDuration;
    }

    public String getLabel() {
        return "Project " + id;
    }

    public List<ExecutionMode> getExecutionModeList() {
        return executionModeList;
    }

    public void setExecutionModeList(List<ExecutionMode> executionModeList) {
        this.executionModeList = executionModeList;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

}
