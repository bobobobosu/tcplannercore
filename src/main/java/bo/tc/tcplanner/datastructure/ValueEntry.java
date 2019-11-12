package bo.tc.tcplanner.datastructure;

import java.util.List;

public class ValueEntry {
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

    public void setWbs(long wbs) {
        this.wbs = wbs;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<HumanStateChange> getHumanStateChangeList() {
        return humanStateChangeList;
    }

    public void setHumanStateChangeList(List<HumanStateChange> humanStateChangeList) {
        this.humanStateChangeList = humanStateChangeList;
    }

    public List<ResourceStateChange> getResourceStateChangeList() {
        return resourceStateChangeList;
    }

    public void setResourceStateChangeList(List<ResourceStateChange> resourceStateChangeList) {
        this.resourceStateChangeList = resourceStateChangeList;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public int getSplittable() {
        return splittable;
    }

    public void setSplittable(int splittable) {
        this.splittable = splittable;
    }

    public int getMovable() {
        return movable;
    }

    public void setMovable(int movable) {
        this.movable = movable;
    }

    public int getChangeable() {
        return changeable;
    }

    public void setChangeable(int changeable) {
        this.changeable = changeable;
    }

    public Double getCapacity() {
        return capacity;
    }

    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }

    public List<ProgressChange> getProgressChangeList() {
        return progressChangeList;
    }

    public void setProgressChangeList(List<ProgressChange> progressChangeList) {
        this.progressChangeList = progressChangeList;
    }
}