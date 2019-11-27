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

import bo.tc.tcplanner.datastructure.ResourceElement;
import bo.tc.tcplanner.domain.solver.comparators.AllocationDifficultyComparator;
import bo.tc.tcplanner.domain.solver.comparators.DelayStrengthComparator;
import bo.tc.tcplanner.domain.solver.comparators.ExecutionModeStrengthComparator;
import bo.tc.tcplanner.domain.solver.comparators.ProgressDeltaStrengthComparator;
import bo.tc.tcplanner.domain.solver.listeners.PredecessorsDoneDateUpdatingVariableListener;
import bo.tc.tcplanner.domain.solver.listeners.PreviousStandstillUpdatingVariableListener;
import bo.tc.tcplanner.domain.solver.listeners.ResourceStateChangeVariableListener;
import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.CountableValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeFactory;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.CustomShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@PlanningEntity(difficultyComparatorClass = AllocationDifficultyComparator.class)
@XStreamAlias("PjsAllocation")
public class Allocation extends AbstractPersistable {

    private Job job;
    private Integer index = 0;
    private Allocation sourceAllocation;
    private Allocation sinkAllocation;
    private AllocationType allocationType;
    // Planning Fact
    private List<Allocation> predecessorAllocationList;
    private List<Allocation> successorAllocationList;
    // Planning variables: changes during planning, between score calculations.
    private ExecutionMode executionMode;
    private Integer delay; // In minutes
    private Integer progressdelta; // out of 100
    // Shadow variables
    private Integer predecessorsDoneDate;
    private String previousStandstill;
    private Integer plannedDuration;
    private Map<String, ResourceElement> resourceElementMap;
    // Force Settings
    private Integer forceStartTime;

    public Allocation() {

    }

    @Override
    public String toString() {
        return this.getId() + "-" + job.getName();
    }

    public Allocation(ExecutionMode executionMode, List<Allocation> listOfAllocation, Integer progressdelta) {
        // Set Basic Information
        this.setJob(executionMode.getJob());
        this.setDelay(0);
        this.setExecutionMode(executionMode);
        this.setId(listOfAllocation.size());
        this.setIndex(listOfAllocation.size());
        this.setPredecessorsDoneDate(0);
        this.setProgressdelta(progressdelta);
        this.setPlannedDuration(this.getExecutionMode().getTimeduration() * this.getProgressdelta() / 100);

        // Initialize Lists
        this.successorAllocationList = new ArrayList<>();
        this.predecessorAllocationList = new ArrayList<>();

        // Update Lists
        listOfAllocation.add(this);
    }

    public Allocation(Job job, int delay, ExecutionMode executionMode, List<Allocation> listOfAllocation) {
        // Set Basic Information
        this.setJob(job);
        this.setDelay(delay);
        this.setExecutionMode(executionMode);
        this.setId(listOfAllocation.size());
        this.setIndex(listOfAllocation.size());
        this.setPredecessorsDoneDate(0);
        this.plannedDuration = executionMode.getTimeduration();
        // Update Lists

    }

    public Allocation(Allocation other) {
        this.job = other.job;
        this.id = other.id;
        this.index = other.index;
        this.sourceAllocation = other.sourceAllocation;
        this.sinkAllocation = other.sinkAllocation;
        this.allocationType = other.allocationType;
        this.predecessorAllocationList = other.predecessorAllocationList;
        this.successorAllocationList = other.successorAllocationList;
        this.executionMode = other.executionMode;
        this.delay = other.delay;
        this.predecessorsDoneDate = other.predecessorsDoneDate;
        this.previousStandstill = other.previousStandstill;
        this.forceStartTime = other.forceStartTime;
        this.plannedDuration = other.getPlannedDuration();
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public void setAllocation(Allocation other) {
        this.job = other.job;
        this.id = other.id;
        this.index = other.index;
        this.sourceAllocation = other.sourceAllocation;
        this.sinkAllocation = other.sinkAllocation;
        this.allocationType = other.allocationType;
        this.predecessorAllocationList = other.predecessorAllocationList;
        this.successorAllocationList = other.successorAllocationList;
        this.executionMode = other.executionMode;
        this.delay = other.delay;
        this.predecessorsDoneDate = other.predecessorsDoneDate;
        this.previousStandstill = other.previousStandstill;
        this.forceStartTime = other.forceStartTime;
        this.plannedDuration = other.getPlannedDuration();
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Allocation getSourceAllocation() {
        return sourceAllocation;
    }

    public void setSourceAllocation(Allocation sourceAllocation) {
        this.sourceAllocation = sourceAllocation;
    }

    public Allocation getSinkAllocation() {
        return sinkAllocation;
    }

    public void setSinkAllocation(Allocation sinkAllocation) {
        this.sinkAllocation = sinkAllocation;
    }

    public List<Allocation> getPredecessorAllocationList() {
        return predecessorAllocationList;
    }

    public void setPredecessorAllocationList(List<Allocation> predecessorAllocationList) {
        this.predecessorAllocationList = predecessorAllocationList;
    }

    public List<Allocation> getSuccessorAllocationList() {
        return successorAllocationList;
    }

    public void setSuccessorAllocationList(List<Allocation> successorAllocationList) {
        this.successorAllocationList = successorAllocationList;
    }

    @PlanningVariable(valueRangeProviderRefs = {
            "executionModeRange"}, strengthComparatorClass = ExecutionModeStrengthComparator.class)
    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(ExecutionMode executionMode) {
        this.executionMode = executionMode;
        this.setJob(executionMode.getJob());
    }

    @PlanningVariable(valueRangeProviderRefs = {
            "delayRange"}, strengthComparatorClass = DelayStrengthComparator.class)
    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    @PlanningVariable(valueRangeProviderRefs = {
            "progressdeltaRange"}, strengthComparatorClass = ProgressDeltaStrengthComparator.class)
    public Integer getProgressdelta() {
        return progressdelta;
    }

    public void setProgressdelta(Integer progressdelta) {
        this.progressdelta = progressdelta;
    }

    @CustomShadowVariable(variableListenerClass = PreviousStandstillUpdatingVariableListener.class, sources = {
            @PlanningVariableReference(variableName = "executionMode")})
    public String getPreviousStandstill() {
        return previousStandstill;
    }

    public void setPreviousStandstill(String previousStandstill) {
        this.previousStandstill = previousStandstill;
    }

    @CustomShadowVariable(variableListenerClass = ResourceStateChangeVariableListener.class, sources = {
            @PlanningVariableReference(variableName = "executionMode"),
            @PlanningVariableReference(variableName = "delay"),
            @PlanningVariableReference(variableName = "progressdelta")})
    public Map<String, ResourceElement> getResourceElementMap() {
        return resourceElementMap;
    }

    public void setResourceElementMap(Map<String, ResourceElement> resourceElementMap) {
        this.resourceElementMap = resourceElementMap;
    }

    @CustomShadowVariable(variableListenerClass = PredecessorsDoneDateUpdatingVariableListener.class,
            sources = {
                    @PlanningVariableReference(variableName = "executionMode"),
                    @PlanningVariableReference(variableName = "delay"),
                    @PlanningVariableReference(variableName = "progressdelta")})
    public Integer getPredecessorsDoneDate() {
        return predecessorsDoneDate;
    }

    public void setPredecessorsDoneDate(Integer predecessorsDoneDate) {
        this.predecessorsDoneDate = predecessorsDoneDate;
    }

    @CustomShadowVariable(variableListenerRef = @PlanningVariableReference(variableName = "predecessorsDoneDate"))
    public Integer getPlannedDuration() {
        return plannedDuration;
    }

    public void setPlannedDuration(Integer plannedDuration) {
        this.plannedDuration = plannedDuration;
    }



    // ************************************************************************
    // Complex methods
    // ************************************************************************
    public Integer getStartDate() {
        if (this.forceStartTime != null)
            return this.forceStartTime;
        if (predecessorsDoneDate == null) {
            return null;
        }
        return predecessorsDoneDate + (delay == null ? 0 : delay);
    }

    public Integer getEndDate() {
        if (predecessorsDoneDate == null) {
            return null;
        }
        return getStartDate() + (delay == null ? 0 : delay) + (plannedDuration == null ? 0 : plannedDuration);
    }

    public Integer getNextStart() {
        if (successorAllocationList.size() > 0) return successorAllocationList.get(0).getStartDate();
        return null;
    }

    public Job getPrevJob() {
        if (predecessorAllocationList.size() > 0) {
            return predecessorAllocationList.get(0).getJob();
        } else {
            return job;
        }
    }

    public Integer getPredecessorGap() {
        if (predecessorAllocationList.size() == 0)
            return 0;
        int realpredecessorsDoneDate = -1;
        Allocation realpredecessor = predecessorAllocationList.get(0);
        while (realpredecessorsDoneDate == -1) {
            if (realpredecessor.getJob().getName() != "dummy" && realpredecessor.getEndDate() != null) {
                realpredecessorsDoneDate = realpredecessor.getEndDate();
            }
            if (realpredecessor.predecessorAllocationList.size() > 0) {
                realpredecessor = realpredecessor.predecessorAllocationList.get(0);
            } else {
                break;
            }

        }
        return Math.abs(getStartDate() - realpredecessorsDoneDate);
    }

    public Integer getSuccessorGap() {
        if (successorAllocationList.size() == 0)
            return 0;
        int realsuccessorStartDate = -1;
        Allocation realsuccessor = successorAllocationList.get(0);
        while (realsuccessorStartDate == -1) {
            if (realsuccessor.getJob().getName() != "dummy" && realsuccessor.getStartDate() != null) {
                realsuccessorStartDate = realsuccessor.getStartDate();
            }
            if (realsuccessor.successorAllocationList.size() > 0) {
                realsuccessor = realsuccessor.successorAllocationList.get(0);
            } else {
                break;
            }

        }
        return Math.abs(realsuccessorStartDate - getEndDate());
    }

    public Integer getDuration() {
        return getEndDate() - getStartDate();
    }

    public Project getProject() {
        return job.getProject();
    }

    public int getProjectCriticalPathEndDate() {
        return job.getProject().getCriticalPathEndDate();
    }

    public JobType getJobType() {
        return getExecutionMode().getJob().getJobType();
    }

    public String getLabel() {
        return "Job " + job.getId();
    }

    // ************************************************************************
    // Ranges
    // ************************************************************************

    @ValueRangeProvider(id = "executionModeRange")
    public List<ExecutionMode> getExecutionModeRange() {
        List<ExecutionMode> executionModes = new ArrayList<>();
        for (ExecutionMode executionMode : executionMode.getJob().getProject().getExecutionModeList()) {
            if (executionMode.getJob().getJobType() == JobType.STANDARD) executionModes.add(executionMode);
        }
        return executionModes;
    }

    @ValueRangeProvider(id = "delayRange")
    public CountableValueRange<Integer> getDelayRange() {
        return ValueRangeFactory.createIntValueRange(0, 200);
    }

    @ValueRangeProvider(id = "progressdeltaRange")
    public CountableValueRange<Integer> getProgressDeltaRange() {
        if (job.getSplittable() == 0) return ValueRangeFactory.createIntValueRange(100, 110, 10);
        return ValueRangeFactory.createIntValueRange(0, 110, 10);
    }


    public AllocationType getAllocationType() {
        return allocationType;
    }

    public void setAllocationType(AllocationType allocationType) {
        this.allocationType = allocationType;
    }

    public Integer getForceStartTime() {
        return forceStartTime;
    }

    public void setForceStartTime(Integer forceStartTime) {
        this.forceStartTime = forceStartTime;
    }


}
