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
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.CountableValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeFactory;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.CustomShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableReference;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static bo.tc.tcplanner.app.DroolsTools.getConstrintedTimeRange;
import static bo.tc.tcplanner.app.TCSchedulingApp.timeEntryMap;
import static bo.tc.tcplanner.datastructure.converters.DataStructureBuilder.dummyJob;

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
    private ZonedDateTime predecessorsDoneDate;
    private String previousStandstill;
    private Duration plannedDuration;
    private Map<String, ResourceElement> resourceElementMap;

    public Allocation() {

    }

    public Allocation(ExecutionMode executionMode, List<Allocation> listOfAllocation, Integer progressdelta) {
        // Set Basic Information
        this.setJob(executionMode.getJob());
        this.setDelay(0);
        this.setExecutionMode(executionMode);
        this.setId(listOfAllocation.size());
        this.setIndex(listOfAllocation.size());
        this.setPredecessorsDoneDate(getProject().getSchedule().getGlobalStartTime());
        this.setProgressdelta(progressdelta);
        this.setPlannedDuration(this.getExecutionMode().getTimeduration().multipliedBy(this.getProgressdelta() / 100));

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
        this.setPredecessorsDoneDate(getProject().getSchedule().getGlobalStartTime());
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
        this.plannedDuration = other.getPlannedDuration();
    }

    @Override
    public String toString() {
        return this.getId() + "-" + job.getName();
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
    public ZonedDateTime getPredecessorsDoneDate() {
        return predecessorsDoneDate;
    }

    public void setPredecessorsDoneDate(ZonedDateTime predecessorsDoneDate) {
        this.predecessorsDoneDate = predecessorsDoneDate;
    }

    @CustomShadowVariable(variableListenerRef = @PlanningVariableReference(variableName = "predecessorsDoneDate"))
    public Duration getPlannedDuration() {
        return plannedDuration;
    }

    public void setPlannedDuration(Duration plannedDuration) {
        this.plannedDuration = plannedDuration;
    }


    // ************************************************************************
    // Complex methods
    // ************************************************************************
    public ZonedDateTime getStartDate() {
        if (predecessorsDoneDate == null) {
            return null;
        }
        if (job.getMovable() == 0 && job.getStartDate() != null) return job.getStartDate();
        return delay == null ? predecessorsDoneDate : predecessorsDoneDate.plusMinutes(delay);
    }

    public ZonedDateTime getEndDate() {
        if (predecessorsDoneDate == null) {
            return null;
        }
        return plannedDuration == null ? getStartDate() : getStartDate().plus(plannedDuration);
    }

    public long getTimeRestrictionScore() {

        Range<ZonedDateTime> thisRange = Range.closed(getStartDate(), getEndDate());
        RangeSet<ZonedDateTime> restrictionRangeSet = getProject().getSchedule().getTimeEntryMap()
                .get(executionMode.getHumanStateChange().getRequirementTimerange());
        RangeSet<ZonedDateTime> overlapRangeSet = restrictionRangeSet.subRangeSet(thisRange);
        if (overlapRangeSet.isEmpty()) {
            Range<ZonedDateTime> containing = restrictionRangeSet.complement().rangeContaining(thisRange.lowerEndpoint());
            return -Math.min(
                    containing.hasLowerBound() ? Duration.between(containing.lowerEndpoint(), thisRange.upperEndpoint()).toMinutes() : Integer.MAX_VALUE,
                    containing.hasUpperBound() ? Duration.between(thisRange.lowerEndpoint(), containing.upperEndpoint()).toMinutes() : Integer.MAX_VALUE);
        } else {
            return overlapRangeSet.asRanges().stream().mapToLong(
                    i -> Duration.between(i.lowerEndpoint(), i.upperEndpoint()).toMinutes()).sum() -
                    plannedDuration.toMinutes();
        }
    }

    public Job getPrevJob() {
        if (predecessorAllocationList.size() > 0) {
            return predecessorAllocationList.get(0).getJob();
        } else {
            return job;
        }
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
        return ValueRangeFactory.createIntValueRange(0, 60 * 24);
    }

    @ValueRangeProvider(id = "progressdeltaRange")
    public CountableValueRange<Integer> getProgressDeltaRange() {
        if (job == dummyJob) return ValueRangeFactory.createIntValueRange(100, 110, 10);
        return ValueRangeFactory.createIntValueRange(0, 110, 1);
    }


    public AllocationType getAllocationType() {
        return allocationType;
    }

    public void setAllocationType(AllocationType allocationType) {
        this.allocationType = allocationType;
    }

}
