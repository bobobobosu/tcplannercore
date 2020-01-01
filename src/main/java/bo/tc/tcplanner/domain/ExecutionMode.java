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

import bo.tc.tcplanner.datastructure.*;
import bo.tc.tcplanner.persistable.AbstractPersistable;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public class ExecutionMode extends AbstractPersistable {
    // Belongs-to relationship
    private transient Schedule schedule;

    // Properties
    private Set<ExecutionModeType> executionModeTypes = new HashSet<>();
    //entries
    private String title;
    private String description;
    private int ExecutionModeIndex;
    //resourceStateChange
    private ResourceStateChange resourceStateChange;
    //humanStateChange
    private HumanStateChange humanStateChange;
    //processChange
    private ProgressChange progressChange;
    //chronological property
    private ChronoProperty chronoProperty;
    //timeline property
    private TimelineProperty timelineProperty;

    public ExecutionMode() {
        super();
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public ExecutionMode setVolatileFlag(boolean volatileFlag) {
        super.setVolatileFlag(volatileFlag);
        return this;
    }

    @Override
    public ExecutionMode removeVolatile() {
        if (resourceStateChange != null) resourceStateChange.removeVolatile();
        if (humanStateChange != null) humanStateChange.removeVolatile();
        if (progressChange != null) progressChange.removeVolatile();
        if (chronoProperty != null) chronoProperty.removeVolatile();
        return this;
    }

    @Override
    public ExecutionMode removeEmpty() {
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

    public Set<ExecutionModeType> getExecutionModeTypes() {
        return executionModeTypes;
    }

    public ExecutionMode setExecutionModeTypes(Set<ExecutionModeType> executionModeTypes) {
        this.executionModeTypes = executionModeTypes;
        return this;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public ExecutionMode setSchedule(Schedule schedule) {
        this.schedule = schedule;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ExecutionMode setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ExecutionMode setDescription(String description) {
        this.description = description;
        return this;
    }

    public TimelineProperty getTimelineProperty() {
        return timelineProperty;
    }

    public ExecutionMode setTimelineProperty(TimelineProperty timelineProperty) {
        this.timelineProperty = timelineProperty;
        return this;
    }

    public boolean isFocused() {
        return this.equals(schedule.special.dummyExecutionMode);
    }
}
