package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;

import java.util.List;

public class TimelineEntry extends AbstractPersistable {
    //names
    String title;
    String description;
    //state changes
    int executionMode;
    HumanStateChange humanStateChange;
    ResourceStateChange resourceStateChange;
    //progress changes
    ProgressChange progressChange;
    //chronological property
    ChronoProperty chronoProperty;
    //timeline property
    TimelineProperty timelineProperty;
    //validation notes
    String score;


    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public TimelineEntry() {
        this.setVolatileFlag(false);
        chronoProperty = new ChronoProperty();
        timelineProperty = new TimelineProperty();
    }

    @Override
    public TimelineEntry removeVolatile() {
        humanStateChange.removeVolatile();
        resourceStateChange.removeVolatile();
        progressChange.removeVolatile();
        chronoProperty.removeVolatile();
        timelineProperty.removeVolatile();
        return this;
    }

    public String getTitle() {
        return title;
    }

    public TimelineEntry setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public TimelineEntry setDescription(String description) {
        this.description = description;
        return this;
    }

    public int getExecutionMode() {
        return executionMode;
    }

    public TimelineEntry setExecutionMode(int executionMode) {
        this.executionMode = executionMode;
        return this;
    }

    public HumanStateChange getHumanStateChange() {
        return humanStateChange;
    }

    public TimelineEntry setHumanStateChange(HumanStateChange humanStateChange) {
        this.humanStateChange = humanStateChange;
        return this;
    }

    public ResourceStateChange getResourceStateChange() {
        return resourceStateChange;
    }

    public TimelineEntry setResourceStateChange(ResourceStateChange resourceStateChange) {
        this.resourceStateChange = resourceStateChange;
        return this;
    }

    public ProgressChange getProgressChange() {
        return progressChange;
    }

    public TimelineEntry setProgressChange(ProgressChange progressChange) {
        this.progressChange = progressChange;
        return this;
    }

    public ChronoProperty getChronoProperty() {
        return chronoProperty;
    }

    public TimelineEntry setChronoProperty(ChronoProperty chronoProperty) {
        this.chronoProperty = chronoProperty;
        return this;
    }

    public TimelineProperty getTimelineProperty() {
        return timelineProperty;
    }

    public TimelineEntry setTimelineProperty(TimelineProperty timelineProperty) {
        this.timelineProperty = timelineProperty;
        return this;
    }
}

