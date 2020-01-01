package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ValueEntry extends AbstractPersistable {
    //identifier
    long wbs;
    String type; // 'task' or 'project' or 'resource'...
    String classification;
    Double capacity;
    //chronological property
    ChronoProperty chronoProperty;
    //state changes
    List<HumanStateChange> humanStateChangeList;
    List<ResourceStateChange> resourceStateChangeList;
    List<ProgressChange> progressChangeList;

    public ValueEntry() {
        super();
    }

    public ValueEntry(ValueEntry valueEntry) {
        super(valueEntry);
        this.wbs = valueEntry.wbs;
        this.type = valueEntry.type;
        this.classification = valueEntry.classification;
        this.capacity = valueEntry.capacity;
        if (valueEntry.chronoProperty != null) this.chronoProperty = new ChronoProperty(valueEntry.chronoProperty);
        if (valueEntry.humanStateChangeList != null)
            this.humanStateChangeList = valueEntry.humanStateChangeList.stream().map(HumanStateChange::new).collect(Collectors.toList());
        if (valueEntry.resourceStateChangeList != null)
            this.resourceStateChangeList = valueEntry.resourceStateChangeList.stream().map(ResourceStateChange::new).collect(Collectors.toList());
        if (valueEntry.progressChangeList != null)
            this.progressChangeList = valueEntry.progressChangeList.stream().map(ProgressChange::new).collect(Collectors.toList());
    }

    public long getWbs() {
        return wbs;
    }

    public ValueEntry setWbs(long wbs) {
        this.wbs = wbs;
        return this;
    }

    public String getType() {
        return type;
    }

    public ValueEntry setType(String type) {
        this.type = type;
        return this;
    }

    public List<HumanStateChange> getHumanStateChangeList() {
        return humanStateChangeList;
    }

    public ValueEntry setHumanStateChangeList(List<HumanStateChange> humanStateChangeList) {
        this.humanStateChangeList = humanStateChangeList;
        return this;
    }

    public List<ResourceStateChange> getResourceStateChangeList() {
        return resourceStateChangeList;
    }

    public ValueEntry setResourceStateChangeList(List<ResourceStateChange> resourceStateChangeList) {
        this.resourceStateChangeList = resourceStateChangeList;
        return this;
    }

    public String getClassification() {
        return classification;
    }

    public ValueEntry setClassification(String classification) {
        this.classification = classification;
        return this;
    }

    public Double getCapacity() {
        return capacity;
    }

    public ValueEntry setCapacity(Double capacity) {
        this.capacity = capacity;
        return this;
    }

    public List<ProgressChange> getProgressChangeList() {
        return progressChangeList;
    }

    public ValueEntry setProgressChangeList(List<ProgressChange> progressChangeList) {
        this.progressChangeList = progressChangeList;
        return this;
    }

    public ChronoProperty getChronoProperty() {
        return chronoProperty;
    }

    public ValueEntry setChronoProperty(ChronoProperty chronoProperty) {
        this.chronoProperty = chronoProperty;
        return this;
    }

    @Override
    public ValueEntry setVolatileFlag(boolean volatileFlag) {
        super.setVolatileFlag(volatileFlag);
        return this;
    }

    @Override
    public ValueEntry removeEmpty() {
        if (humanStateChangeList != null) {
            humanStateChangeList.forEach(AbstractPersistable::removeEmpty);
        }
        if (progressChangeList != null) {
            progressChangeList.forEach(AbstractPersistable::removeEmpty);
        }
        if (resourceStateChangeList != null) {
            resourceStateChangeList.forEach(AbstractPersistable::removeEmpty);
        }

        return this;
    }

    @Override
    public ValueEntry removeVolatile() {
        if (humanStateChangeList != null) {
            humanStateChangeList.removeIf(AbstractPersistable::isVolatileFlag);
            humanStateChangeList.forEach(HumanStateChange::removeVolatile);
        }
        if (progressChangeList != null) {
            progressChangeList.removeIf(AbstractPersistable::isVolatileFlag);
            progressChangeList.forEach(ProgressChange::removeVolatile);
        }
        if (resourceStateChangeList != null) {
            resourceStateChangeList.removeIf(AbstractPersistable::isVolatileFlag);
            resourceStateChangeList.forEach(ResourceStateChange::removeVolatile);
        }
        return this;
    }

}