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

import bo.tc.tcplanner.datastructure.HumanStateChange;
import bo.tc.tcplanner.datastructure.ProgressChange;
import bo.tc.tcplanner.datastructure.ResourceStateChange;
import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.List;

@XStreamAlias("PjsExecutionMode")
public class ExecutionMode extends AbstractPersistable {
    @Override
    public String toString() {
        return getJob().getName();
    }

    private Job job;
    private int ExecutionModeIndex;


    //resourceStateChange
    private ResourceStateChange resourceStateChange;

    //humanStateChange
    private HumanStateChange humanStateChange;

    //processChange
    private ProgressChange progressChange;


    public ExecutionMode(HumanStateChange humanStateChange, List<ExecutionMode> listOfExecutionMode, Job job) {
        //Set Basic Information
        this.setJob(job);
        this.setId(listOfExecutionMode.size());
        this.setHumanStateChange(humanStateChange);

        //Initialize
        resourceStateChange = new ResourceStateChange();

        //Update List
        listOfExecutionMode.add(this);
    }

    public ExecutionMode(Job job, HumanStateChange humanStateChange, List<ExecutionMode> listOfExecutionMode) {
        this.setJob(job);
        this.setId(listOfExecutionMode.size());
        this.setHumanStateChange(humanStateChange);
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
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

    public int getTimeduration() {
        return (int) humanStateChange.getDuration();
    }


    public int getExecutionModeIndex() {
        return ExecutionModeIndex;
    }

    public void setExecutionModeIndex(int executionModeIndex) {
        ExecutionModeIndex = executionModeIndex;
    }


    public ResourceStateChange getResourceStateChange() {
        return resourceStateChange;
    }

    public void setResourceStateChange(ResourceStateChange resourceStateChange) {
        this.resourceStateChange = resourceStateChange;
    }

    public HumanStateChange getHumanStateChange() {
        return humanStateChange;
    }

    public void setHumanStateChange(HumanStateChange humanStateChange) {
        this.humanStateChange = humanStateChange;
    }

    public ProgressChange getProgressChange() {
        return progressChange;
    }

    public void setProgressChange(ProgressChange progressChange) {
        this.progressChange = progressChange;
    }
}
