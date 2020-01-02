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

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.datastructure.ResourceElement;
import bo.tc.tcplanner.datastructure.TimelineEntry;
import bo.tc.tcplanner.domain.solver.comparators.DelayStrengthComparator;
import bo.tc.tcplanner.domain.solver.comparators.ProgressDeltaStrengthComparator;
import bo.tc.tcplanner.domain.solver.comparators.TimelineEntryStrengthComparator;
import bo.tc.tcplanner.domain.solver.listeners.PredecessorsDoneDateUpdatingVariableListener;
import bo.tc.tcplanner.domain.solver.listeners.PreviousStandstillUpdatingVariableListener;
import bo.tc.tcplanner.domain.solver.listeners.ResourceStateChangeVariableListener;
import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.entity.PlanningPin;
import org.optaplanner.core.api.domain.valuerange.CountableValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeFactory;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.CustomShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableReference;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@PlanningEntity
public class Allocation extends AbstractPersistable {
    // Belongs-to relationship
    private transient Schedule schedule;

    // Pre-Solving Properties
    private int index;

    // Planning variables: changes during planning, between score calculations.
    private TimelineEntry timelineEntry;
    private Integer delay; // In minutes
    private Integer progressdelta; // out of 100
    // Shadow variables
    private ZonedDateTime predecessorsDoneDate;
    private String previousStandstill;
    private Duration plannedDuration;
    private Map<String, List<ResourceElement>> resourceElementMap;

    public Allocation() {

    }

    @Override
    public Allocation removeVolatile() {
        timelineEntry.removeVolatile();
        if (resourceElementMap != null)
            resourceElementMap.forEach((k, v) -> v.forEach(ResourceElement::removeVolatile));

        return this;
    }

    @Override
    public Allocation removeEmpty() {
        if (resourceElementMap != null)
            resourceElementMap.entrySet().removeIf(x -> x.getValue().size() == 0);

        return this;
    }

    @Override
    public boolean checkValid() {
        checkNotNull(schedule);
        checkNotNull(timelineEntry);
        checkNotNull(delay);
        checkArgument(progressdelta >= 0);
        checkArgument(progressdelta <= 100);
        checkArgument(schedule.checkValid());
        checkArgument(timelineEntry.checkValid());
        return true;
    }

    @Override
    public String toString() {
        return this.getIndex() + "-" + timelineEntry;
    }

    @Override
    public boolean isVolatileFlag() {
        if (!timelineEntry.isVolatileFlag()) return false;
        return this.volatileFlag;
    }

    @Override
    public Allocation setVolatileFlag(boolean volatileFlag) {
        super.setVolatileFlag(volatileFlag);
        return this;
    }

    @PlanningVariable(valueRangeProviderRefs = {
            "timelineEntryRange"}, strengthComparatorClass = TimelineEntryStrengthComparator.class)
    public TimelineEntry getTimelineEntry() {
        return timelineEntry;
    }

    public void setTimelineEntry(TimelineEntry timelineEntry) {
        this.timelineEntry = timelineEntry;
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
            @PlanningVariableReference(variableName = "timelineEntry")})
    public String getPreviousStandstill() {
        return previousStandstill;
    }

    public void setPreviousStandstill(String previousStandstill) {
        this.previousStandstill = previousStandstill;
    }

    @CustomShadowVariable(variableListenerClass = ResourceStateChangeVariableListener.class, sources = {
            @PlanningVariableReference(variableName = "timelineEntry"),
            @PlanningVariableReference(variableName = "delay"),
            @PlanningVariableReference(variableName = "progressdelta")})
    public Map<String, List<ResourceElement>> getResourceElementMap() {
        return resourceElementMap;
    }

    public void setResourceElementMap(Map<String, List<ResourceElement>> resourceElementMap) {
        this.resourceElementMap = resourceElementMap;
    }

    @CustomShadowVariable(variableListenerClass = PredecessorsDoneDateUpdatingVariableListener.class,
            sources = {
                    @PlanningVariableReference(variableName = "timelineEntry"),
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

    @PlanningPin
    public boolean isPinned() {
        return timelineEntry.getTimelineProperty().getPlanningWindowType()
                .equals(PropertyConstants.PlanningWindowTypes.types.History.name());
    }


    // ************************************************************************
    // Scores
    // ************************************************************************

    public Double getResourceElementMapUtilizationScore() {
        double score = 0;
        for (Map.Entry<String, List<ResourceElement>> entry : resourceElementMap.entrySet()) {
            for (ResourceElement resourceElement : entry.getValue()) {
                score += (resourceElement.getAmt() > 0) ? resourceElement.getAmt() : 0;
            }
        }
        return -score;
    }

    public Double getResourceElementMapExcessScore() {
        double score = 0;
        for (Map.Entry<String, List<ResourceElement>> entry : resourceElementMap.entrySet()) {
            int alive = 0;
            double capacity = schedule.getValueEntryMap().get(entry.getKey()).getCapacity();
            for (ResourceElement resourceElement : entry.getValue()) {
                alive += (resourceElement.getType().equals("production")) ? resourceElement.getAmt() : 0;
            }
            score += (alive > capacity) ? capacity - alive : 0;
        }
        return score;
    }

    public Double getResourceElementMapDeficitScore() {
        double score = 0;
        for (Map.Entry<String, List<ResourceElement>> entry : resourceElementMap.entrySet()) {
            for (ResourceElement resourceElement : entry.getValue()) {
                score += (resourceElement.getAmt() < 0) ? resourceElement.getAmt() : 0;
            }
        }
        return score;
    }

    public long getTimeRestrictionScore() {
        Range<ZonedDateTime> thisRange = Range.closed(getStartDate(), getEndDate());
        RangeSet<ZonedDateTime> restrictionRangeSet = schedule.getTimeEntryMap()
                .get(timelineEntry.getHumanStateChange().getRequirementTimerange());
        RangeSet<ZonedDateTime> overlapRangeSet = restrictionRangeSet.subRangeSet(thisRange);
        if (restrictionRangeSet.isEmpty()) return plannedDuration.toMinutes();
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

    // ************************************************************************
    // Ranges
    // ************************************************************************

    @ValueRangeProvider(id = "timelineEntryRange")
    public List<TimelineEntry> getTimelineEntryRange() {
//        return schedule.getTimelineEntryList();
        return schedule.getTimelineEntryList().stream().filter(x -> x.getTimelineProperty().getPlanningWindowType()
                .equals(PropertyConstants.PlanningWindowTypes.types.Draft.name())).collect(Collectors.toList());
    }

    @ValueRangeProvider(id = "delayRange")
    public CountableValueRange<Integer> getDelayRange() {
        if (timelineEntry.equals(schedule.special.dummyTimelineEntry))
            return ValueRangeFactory.createIntValueRange(0, 1, 1);
        return ValueRangeFactory.createIntValueRange(0, 60 * 24);
    }

    @ValueRangeProvider(id = "progressdeltaRange")
    public CountableValueRange<Integer> getProgressDeltaRange() {
        if (timelineEntry.equals(schedule.special.dummyTimelineEntry))
            return ValueRangeFactory.createIntValueRange(100, 110, 10);
        return ValueRangeFactory.createIntValueRange(0, 101, 1);
    }


    // ************************************************************************
    // Complex methods
    // ************************************************************************
    public ZonedDateTime getStartDate() {
        if (predecessorsDoneDate == null) {
            return null;
        }

        if (timelineEntry.getChronoProperty().getMovable() == 0 &&
                timelineEntry.getChronoProperty().getZonedStartTime() != null) {
            return timelineEntry.getChronoProperty().getZonedStartTime();
        }
        return delay == null ? predecessorsDoneDate : predecessorsDoneDate.plusMinutes(delay);
    }

    public ZonedDateTime getEndDate() {
        if (predecessorsDoneDate == null) {
            return null;
        }
        return plannedDuration == null ? getStartDate() : getStartDate().plus(plannedDuration);
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public Allocation setSchedule(Schedule schedule) {
        this.schedule = schedule;
        return this;
    }

    public Allocation getNextFocusedAllocation() {
        Allocation result = null;
        for (int i = index + 1; i < schedule.getAllocationList().size(); i++) {
            if (schedule.getAllocationList().get(i).isFocused()) {
                result = schedule.getAllocationList().get(i);
                break;
            }
        }
        return result;
    }

    public Allocation getPrevFocusedAllocation() {
        Allocation result = null;
        for (int i = index - 1; i >= 0; i--) {
            if (schedule.getAllocationList().get(i).isFocused()) {
                result = schedule.getAllocationList().get(i);
                break;
            }
        }
        return result;
    }

    public Iterator<Allocation> getFocusedAllocationsTillEndIterator() {
        Allocation allocation = this;
        Allocation tmpAllocation;
        // Go back two allocations
        if ((tmpAllocation = allocation.getPrevFocusedAllocation()) != null) allocation = tmpAllocation;
        if ((tmpAllocation = allocation.getPrevFocusedAllocation()) != null) allocation = tmpAllocation;
        Allocation finalAllocation = allocation;
        return new Iterator<Allocation>() {
            Allocation thisAllocation = finalAllocation;

            @Override
            public boolean hasNext() {
                return thisAllocation.getNextFocusedAllocation() != null;
            }

            @Override
            public Allocation next() {
                return (thisAllocation = thisAllocation.getNextFocusedAllocation());
            }
        };
    }

    public List<Allocation> getFocusedAllocationsTillEnd() {
        List<Allocation> focusedAllocationsTillEnd = new LinkedList<>();
        Allocation allocation = this;
        Allocation tmpAllocation = null;
        // Go back two allocations
        if ((tmpAllocation = allocation.getPrevFocusedAllocation()) != null) allocation = tmpAllocation;
        if ((tmpAllocation = allocation.getPrevFocusedAllocation()) != null) allocation = tmpAllocation;
        while ((allocation = allocation.getNextFocusedAllocation()) != null) {
            focusedAllocationsTillEnd.add(allocation);
        }
        return focusedAllocationsTillEnd;
    }

    public Integer getIndex() {
        return index;
    }

    public Allocation setIndex(Integer index) {
        this.index = index;
        return this;
    }


    public boolean isOld() {
        return timelineEntry.getTimelineProperty().getPlanningWindowType().equals(PropertyConstants.PlanningWindowTypes.types.Published.name()) ||
                timelineEntry.getTimelineProperty().getPlanningWindowType().equals(PropertyConstants.PlanningWindowTypes.types.History.name());
    }

    public boolean isHistory() {
        return timelineEntry.getTimelineProperty().getPlanningWindowType().equals(PropertyConstants.PlanningWindowTypes.types.History.name());
    }

    public boolean isFocused() {
        return this.equals(schedule.special.sourceAllocation) ||
                this.equals(schedule.special.sinkAllocation) ||
                !timelineEntry.equals(schedule.special.dummyTimelineEntry);
    }
}
