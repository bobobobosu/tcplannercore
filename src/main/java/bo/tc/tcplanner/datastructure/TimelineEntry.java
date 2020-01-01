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

    public TimelineEntry() {
        super();
    }

    public TimelineEntry(TimelineEntry timelineEntry) {
        super(timelineEntry);
        this.title = timelineEntry.title;
        this.description = timelineEntry.description;
        this.executionMode = timelineEntry.executionMode;
        if (timelineEntry.humanStateChange != null)
            this.humanStateChange = new HumanStateChange(timelineEntry.humanStateChange);
        if (timelineEntry.resourceStateChange != null)
            this.resourceStateChange = new ResourceStateChange(timelineEntry.resourceStateChange);
        if (timelineEntry.progressChange != null)
            this.progressChange = new ProgressChange(timelineEntry.progressChange);
        if (timelineEntry.chronoProperty != null)
            this.chronoProperty = new ChronoProperty(timelineEntry.chronoProperty);
        if (timelineEntry.timelineProperty != null)
            this.timelineProperty = new TimelineProperty(timelineEntry.timelineProperty);
        this.score = timelineEntry.score;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }


    @Override
    public TimelineEntry removeVolatile() {
        if (humanStateChange != null) humanStateChange.removeVolatile();
        if (resourceStateChange != null) resourceStateChange.removeVolatile();
        if (progressChange != null) progressChange.removeVolatile();
        if (chronoProperty != null) chronoProperty.removeVolatile();
        if (timelineProperty != null) timelineProperty.removeVolatile();
        return this;
    }

    @Override
    public AbstractPersistable removeEmpty() {
        if (humanStateChange != null) humanStateChange.removeEmpty();
        if (resourceStateChange != null) resourceStateChange.removeEmpty();
        if (progressChange != null) progressChange.removeEmpty();
        if (chronoProperty != null) chronoProperty.removeEmpty();
        if (timelineProperty != null) timelineProperty.removeEmpty();
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

