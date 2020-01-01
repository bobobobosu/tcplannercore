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
import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactProperty;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.persistence.xstream.api.score.buildin.bendable.BendableScoreXStreamConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@PlanningSolution
public class Schedule extends AbstractPersistable {

    // Domain
    private List<Allocation> allocationList;
    private List<ExecutionMode> executionModeList;

    // Objects
    private TimelineBlock problemTimelineBlock;
    private ValueEntryMap valueEntryMap;
    private TimeEntryMap timeEntryMap;
    public Special special;

    public class Special {
        public Allocation sourceAllocation;
        public Allocation sinkAllocation;
        public ExecutionMode dummyExecutionMode;
        public HumanStateChange dummyHumamStateChange;
        public ProgressChange dummyProgressChange;
        public ResourceStateChange dummyResourceStateChange;
        public ChronoProperty dummyChronoProperty;
        public TimelineProperty dummyTimelineProperty;
        public String dummyLocation;
        public String dummyTime;
    }


    @Override
    public Schedule removeVolatile() {
        executionModeList.removeIf(AbstractPersistable::isVolatileFlag);
        executionModeList.forEach(ExecutionMode::removeVolatile);
        valueEntryMap.forEach((k, v) -> v.removeVolatile());
        valueEntryMap.entrySet().removeIf(x -> x.getValue().isVolatileFlag());
        allocationList.removeIf(AbstractPersistable::isVolatileFlag);
        allocationList.forEach(Allocation::removeVolatile);
        return this;
    }

    @Override
    public AbstractPersistable removeEmpty() {
        return this;
    }

    @XStreamConverter(BendableScoreXStreamConverter.class)
    private BendableScore score;


    public Schedule() {
        //Initialize
        executionModeList = new ArrayList<>();
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
    public List<ExecutionMode> getExecutionModeList() {
        return executionModeList;
    }

    public Schedule setExecutionModeList(List<ExecutionMode> executionModeList) {
        this.executionModeList = executionModeList;
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

    public ExecutionMode getDummyExecutionMode() {
        return special.dummyExecutionMode;
    }

    public Schedule setDummyExecutionMode(ExecutionMode dummyExecutionMode) {
        this.special.dummyExecutionMode = dummyExecutionMode;
        return this;
    }

    public List<Allocation> getFocusedAllocationList() {
        return allocationList.stream().filter(Allocation::isFocused).collect(Collectors.toList());
    }

    public List<Allocation> getDummyAllocationList() {
        List<Allocation> dummyAllocationList = new ArrayList<>();
        Allocation prevAllocation = null;
        for (Allocation thisAllocaion : allocationList) {
            if (thisAllocaion.isFocused()) {
                if (prevAllocation != null && (thisAllocaion.getIndex() - prevAllocation.getIndex() > 1))
                    dummyAllocationList.add(
                            allocationList.get((thisAllocaion.getIndex() + prevAllocation.getIndex()) / 2)
                    );
                prevAllocation = thisAllocaion;
            }
        }
        return dummyAllocationList;
    }

}
