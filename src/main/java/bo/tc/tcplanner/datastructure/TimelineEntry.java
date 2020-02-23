package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jetbrains.annotations.Nullable;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@JsonIgnoreProperties(ignoreUnknown = true)

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
    @Nullable
    String score;

    public TimelineEntry() {
        super();
    }

    public TimelineEntry(TimelineEntry timelineEntry) {
        super(timelineEntry);
        this.title = timelineEntry.title;
        this.description = timelineEntry.description;
        this.executionMode = timelineEntry.executionMode;
        this.humanStateChange = new HumanStateChange(timelineEntry.humanStateChange);
        this.resourceStateChange = new ResourceStateChange(timelineEntry.resourceStateChange);
        this.progressChange = new ProgressChange(timelineEntry.progressChange);
        this.chronoProperty = new ChronoProperty(timelineEntry.chronoProperty);
        this.timelineProperty = new TimelineProperty(timelineEntry.timelineProperty);
        if (timelineEntry.score != null) this.score = timelineEntry.score;
    }

    @Override
    public boolean checkValid() {
        checkNotNull(title);
        checkNotNull(description);
        checkArgument(executionMode >= 0);
        checkNotNull(humanStateChange);
        checkNotNull(resourceStateChange);
        checkNotNull(progressChange);
        checkNotNull(chronoProperty);
        checkNotNull(timelineProperty);
        checkArgument(humanStateChange.checkValid());
        checkArgument(resourceStateChange.checkValid());
        checkArgument(progressChange.checkValid());
        checkArgument(chronoProperty.checkValid());
        checkArgument(timelineProperty.checkValid());
        return true;
    }


    @Override
    public TimelineEntry setVolatileFlag(boolean volatileFlag) {
        super.setVolatileFlag(volatileFlag);
        return this;
    }

    @Override
    public String toString() {
        return timelineProperty.getRownum() + " " + title;
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


    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TimelineEntry that = (TimelineEntry) o;

        if (executionMode != that.executionMode) return false;
        if (!title.equals(that.title)) return false;
        if (!description.equals(that.description)) return false;
        if (!humanStateChange.equals(that.humanStateChange)) return false;
        if (!resourceStateChange.equals(that.resourceStateChange)) return false;
        if (!progressChange.equals(that.progressChange)) return false;
        if (!chronoProperty.equals(that.chronoProperty)) return false;
        return timelineProperty.equals(that.timelineProperty);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + executionMode;
        result = 31 * result + humanStateChange.hashCode();
        result = 31 * result + resourceStateChange.hashCode();
        result = 31 * result + progressChange.hashCode();
        result = 31 * result + chronoProperty.hashCode();
        result = 31 * result + timelineProperty.hashCode();
        return result;
    }

//    @JsonIgnore
//    List<Allocation> allocatedList = new ArrayList<>();
//
//    @InverseRelationShadowVariable(sourceVariableName = "timelineEntry")
//    public List<Allocation> getAllocatedList() {
//        return allocatedList;
//    }
//
//    public void setAllocatedList(List<Allocation> allocatedList) {
//        this.allocatedList = allocatedList;
//    }

}

