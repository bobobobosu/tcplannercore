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

import bo.tc.tcplanner.datastructure.TimelineProperty;
import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@XStreamAlias("PjsJob")
public class Job extends AbstractPersistable {
    private String name;
    private String description = "";
    private Project project;
    private JobType jobType;

    private TimelineProperty timelineProperty;
    private List<ExecutionMode> executionModeList;

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

        //Initialize
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


    public List<ExecutionMode> getExecutionModeList() {
        return executionModeList;
    }

    public Job setExecutionModeList(List<ExecutionMode> executionModeList) {
        this.executionModeList = executionModeList;
        return this;
    }

    public TimelineProperty getTimelineProperty() {
        return timelineProperty;
    }

    public Job setTimelineProperty(TimelineProperty timelineProperty) {
        this.timelineProperty = timelineProperty;
        return this;
    }
}
