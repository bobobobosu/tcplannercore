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

import bo.tc.tcplanner.datastructure.*;
import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactProperty;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@PlanningSolution
public class Schedule extends AbstractPersistable {

    // Domain
    private List<Allocation> allocationList;
    private List<TimelineEntry> timelineEntryList;

    // Objects
    private TimelineBlock problemTimelineBlock;
    private ValueEntryMap valueEntryMap;
    private TimeEntryMap timeEntryMap;
    public Special special;

    // Easy Access
    private Map<TimelineEntry, TimelineEntry> job2jobcloneMap;
    public TreeSet<Allocation> focusedAllocationSet;

    // Settings
    public String valueRangeMode = "default";

    public class Special {
        public HumanStateChange dummyHumamStateChange;
        public ProgressChange dummyProgressChange;
        public ResourceStateChange dummyResourceStateChange;
        public ChronoProperty dummyChronoProperty;
        public TimelineProperty dummyTimelineProperty;
        public String dummyLocation;
        public String dummyTime;
    }

    public Allocation getSourceAllocation() {
        return allocationList.get(0);
    }

    public Allocation getSinkAllocation() {
        return allocationList.get(allocationList.size() - 1);
    }

    public TimelineEntry getDummyTimelineEntry() {
        return timelineEntryList.get(0);
    }


    @Override
    public boolean checkValid() {
        checkNotNull(allocationList);
        checkNotNull(timelineEntryList);
        checkNotNull(problemTimelineBlock);
        checkNotNull(valueEntryMap);
        checkNotNull(timeEntryMap);
        checkNotNull(job2jobcloneMap);
        checkArgument(allocationList.stream().allMatch(Allocation::checkValid));
        checkArgument(timelineEntryList.stream().allMatch(TimelineEntry::checkValid));
        checkArgument(job2jobcloneMap.entrySet().stream().allMatch(x -> x.getValue().getResourceStateChange().equals(x.getKey().getResourceStateChange())));
        checkArgument(valueEntryMap.checkValid());
        return true;
    }

    @Override
    public Schedule removeVolatile() {
        timelineEntryList.removeIf(AbstractPersistable::isVolatileFlag);
        timelineEntryList.forEach(TimelineEntry::removeVolatile);
        valueEntryMap.forEach((k, v) -> v.removeVolatile());
        valueEntryMap.entrySet().removeIf(x -> x.getValue().isVolatileFlag());
        allocationList.removeIf(AbstractPersistable::isVolatileFlag);
        allocationList.forEach(Allocation::removeVolatile);
        job2jobcloneMap.entrySet().removeIf(x -> x.getKey().isVolatileFlag() || x.getValue().isVolatileFlag());
        return this;
    }

    @Override
    public AbstractPersistable removeEmpty() {
        return this;
    }

    //    @XStreamConverter(BendableScoreXStreamConverter.class)
    private BendableScore score;


    public Schedule() {
        //Initialize
        timelineEntryList = new ArrayList<TimelineEntry>();
        allocationList = new ArrayList<>();
        special = new Special();
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

    public Schedule setTimeEntryMap(TimeEntryMap timeEntryMap) {
        this.timeEntryMap = timeEntryMap;
        return this;
    }

    @ProblemFactCollectionProperty
    public List<TimelineEntry> getTimelineEntryList() {
        return timelineEntryList;
    }

    public Schedule setTimelineEntryList(List<TimelineEntry> timelineEntryList) {
        this.timelineEntryList = timelineEntryList;
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


    public TimelineBlock getProblemTimelineBlock() {
        return problemTimelineBlock;
    }

    public Schedule setProblemTimelineBlock(TimelineBlock problemTimelineBlock) {
        this.problemTimelineBlock = problemTimelineBlock;
        return this;
    }

    public Map<TimelineEntry, TimelineEntry> getJob2jobcloneMap() {
        return job2jobcloneMap;
    }

    public Schedule setJob2jobcloneMap(Map<TimelineEntry, TimelineEntry> job2jobcloneMap) {
        this.job2jobcloneMap = job2jobcloneMap;
        return this;
    }

    public Iterator<Allocation> getDummyAllocationIterator() {
        return new Iterator<Allocation>() {
            Iterator<Allocation> focusedAllocationIterator = focusedAllocationSet.iterator();
            Allocation thisAllocation = focusedAllocationIterator.next();
            Allocation nextAllocation = focusedAllocationIterator.hasNext() ? getDummyAllocation(
                    thisAllocation, thisAllocation = focusedAllocationIterator.next()) : null;

            @Override
            public boolean hasNext() {
                return nextAllocation != null;
            }

            @Override
            public Allocation next() {
                Allocation saveAllocation = nextAllocation;
                nextAllocation = focusedAllocationIterator.hasNext() ? getDummyAllocation(
                        thisAllocation, thisAllocation = focusedAllocationIterator.next()) : null;
                return saveAllocation;
            }

            private Allocation getDummyAllocation(Allocation prevAllocation, Allocation thisAllocation) {
                if (prevAllocation.equals(thisAllocation)) return null;
                if (thisAllocation.getIndex() - prevAllocation.getIndex() < 2) return null;
                return allocationList.get((thisAllocation.getIndex() + prevAllocation.getIndex()) / 2);
            }
        };
    }

    public List<Allocation> getDummyAllocationList() {
        return Lists.newArrayList(getDummyAllocationIterator());
    }

    public Iterator<Allocation> getCondensedAllocationIterator() {
        return Iterators.concat(getDummyAllocationIterator(), focusedAllocationSet.iterator());
    }

    public List<Allocation> getFocusedAllocationList() {
        return allocationList.stream().filter(Allocation::isFocused).collect(Collectors.toList());
    }

    public List<Allocation> getBriefAllocationList() {
        List<Allocation> briefAllocations = new ArrayList<>();
        Iterator<Allocation> viewAllocationsIterator = focusedAllocationSet.iterator();
        while (viewAllocationsIterator.hasNext()) {
            Allocation allocation = viewAllocationsIterator.next();
            if (briefAllocations.size() > 0) {
                int prevIdx = briefAllocations.get(briefAllocations.size() - 1).getIndex();
                int thisIdx = allocation.getIndex();
                if ((prevIdx + thisIdx) / 2 != prevIdx) {
                    briefAllocations.add(getAllocationList().get((prevIdx + thisIdx) / 2));
                }
            }
            briefAllocations.add(allocation);
        }
        return briefAllocations;
    }
}
