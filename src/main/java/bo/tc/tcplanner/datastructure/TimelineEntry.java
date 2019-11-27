package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;

import java.util.List;

public class TimelineEntry extends AbstractPersistable {
    //names
    String title;
    String description;
    //state changes
    int taskMode;
    HumanStateChange humanStateChange;
    ResourceStateChange resourceStateChange;
    //progress changes
    ProgressChange progressChange;
    //chronological property
    String startTime;
    String deadline;
    int movable;
    int gravity;
    int splittable;
    int changeable;
    Integer rownum;
    List<Integer> dependencyIdList;

    public TimelineEntry(){
        this.setVolatileFlag(false);
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

    public int getTaskMode() {
        return taskMode;
    }

    public TimelineEntry setTaskMode(int taskMode) {
        this.taskMode = taskMode;
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

    public String getStartTime() {
        return startTime;
    }

    public TimelineEntry setStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    public String getDeadline() {
        return deadline;
    }

    public TimelineEntry setDeadline(String deadline) {
        this.deadline = deadline;
        return this;
    }

    public int getMovable() {
        return movable;
    }

    public TimelineEntry setMovable(int movable) {
        this.movable = movable;
        return this;
    }

    public int getGravity() {
        return gravity;
    }

    public TimelineEntry setGravity(int gravity) {
        this.gravity = gravity;
        return this;
    }

    public Integer getRownum() {
        return rownum;
    }

    public TimelineEntry setRownum(Integer rownum) {
        this.rownum = rownum;
        return this;
    }

    public List<Integer> getDependencyIdList() {
        return dependencyIdList;
    }

    public TimelineEntry setDependencyIdList(List<Integer> dependencyIdList) {
        this.dependencyIdList = dependencyIdList;
        return this;
    }

    public int getSplittable() {
        return splittable;
    }

    public TimelineEntry setSplittable(int splittable) {
        this.splittable = splittable;
        return this;
    }

    public int getChangeable() {
        return changeable;
    }

    public TimelineEntry setChangeable(int changeable) {
        this.changeable = changeable;
        return this;
    }
}

