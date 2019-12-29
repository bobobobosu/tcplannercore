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

import bo.tc.tcplanner.datastructure.ChronoProperty;
import bo.tc.tcplanner.datastructure.HumanStateChange;
import bo.tc.tcplanner.datastructure.ProgressChange;
import bo.tc.tcplanner.datastructure.ResourceStateChange;
import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@XStreamAlias("PjsExecutionMode")
public class ExecutionMode extends AbstractPersistable {
    private Job job;
    private int ExecutionModeIndex;
    private Set<ExecutionModeType> executionModeTypes = new HashSet<>();
    //resourceStateChange
    private ResourceStateChange resourceStateChange;
    //humanStateChange
    private HumanStateChange humanStateChange;
    //processChange
    private ProgressChange progressChange;
    //chronological property
    private ChronoProperty chronoProperty;

    //easy access
    private ZonedDateTime startDate = null;
    private ZonedDateTime deadline = null;

    public ExecutionMode(List<ExecutionMode> listOfExecutionMode, Job job) {
        //Set Basic Information
        this.setJob(job);
        this.setId(listOfExecutionMode.size());

        //Initialize
        resourceStateChange = new ResourceStateChange();

        //Update List
        listOfExecutionMode.add(this);
    }


    public ExecutionMode(Job job, List<ExecutionMode> listOfExecutionMode) {
        this.setJob(job);
        this.setId(listOfExecutionMode.size());
    }

    @Override
    public String toString() {
        return getJob().getName();
    }

    public Job getJob() {
        return job;
    }

    public ExecutionMode setJob(Job job) {
        this.job = job;
        return this;
    }


    public String getCurrentLocation() {
        return humanStateChange.getCurrentLocation();
    }


    public String getMovetoLocation() {
        return humanStateChange.getMovetoLocation();
    }

    public String getRequirementTimerange() {
        return humanStateChange.getRequirementTimerange();
    }

    public Duration getTimeduration() {
        return Duration.ofMinutes((long) humanStateChange.getDuration());
    }


    public int getExecutionModeIndex() {
        return ExecutionModeIndex;
    }

    public ExecutionMode setExecutionModeIndex(int executionModeIndex) {
        ExecutionModeIndex = executionModeIndex;
        return this;
    }


    public ResourceStateChange getResourceStateChange() {
        return resourceStateChange;
    }

    public ExecutionMode setResourceStateChange(ResourceStateChange resourceStateChange) {
        this.resourceStateChange = resourceStateChange;
        return this;
    }

    public HumanStateChange getHumanStateChange() {
        return humanStateChange;
    }

    public ExecutionMode setHumanStateChange(HumanStateChange humanStateChange) {
        this.humanStateChange = humanStateChange;
        return this;
    }

    public ProgressChange getProgressChange() {
        return progressChange;
    }

    public ExecutionMode setProgressChange(ProgressChange progressChange) {
        this.progressChange = progressChange;
        return this;
    }

    public ChronoProperty getChronoProperty() {
        return chronoProperty;
    }

    public ExecutionMode setChronoProperty(ChronoProperty chronoProperty) {
        this.chronoProperty = chronoProperty;
        return this;
    }

    public ZonedDateTime getStartDate() {
        if (chronoProperty.getStartTime() == null) return null;
        if (startDate == null) startDate = ZonedDateTime.parse(chronoProperty.getStartTime());
        return startDate;
    }

    public ZonedDateTime getDeadline() {
        if (chronoProperty.getDeadline() == null) return null;
        if (deadline == null) deadline = ZonedDateTime.parse(chronoProperty.getDeadline());
        return deadline;
    }

    public Set<ExecutionModeType> getExecutionModeTypes() {
        return executionModeTypes;
    }

    public ExecutionMode setExecutionModeTypes(Set<ExecutionModeType> executionModeTypes) {
        this.executionModeTypes = executionModeTypes;
        return this;
    }
}
