package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;

import java.util.List;

public class ValueEntry extends AbstractPersistable {
    //identifier
    long wbs;
    String type; // 'task' or 'project' or 'resource'...
    String classification;
    int splittable;
    int movable;
    int changeable;
    Double capacity;
    //state changes
    List<HumanStateChange> humanStateChangeList;
    List<ResourceStateChange> resourceStateChangeList;
    List<ProgressChange> progressChangeList;

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

    public int getSplittable() {
        return splittable;
    }

    public ValueEntry setSplittable(int splittable) {
        this.splittable = splittable;
        return this;
    }

    public int getMovable() {
        return movable;
    }

    public ValueEntry setMovable(int movable) {
        this.movable = movable;
        return this;
    }

    public int getChangeable() {
        return changeable;
    }

    public ValueEntry setChangeable(int changeable) {
        this.changeable = changeable;
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
}