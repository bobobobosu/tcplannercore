package bo.tc.tcplanner.datastructure;

import java.util.List;

public class TimelineEntry {
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
    Integer id;
    Integer rownum;
    List<Integer> dependencyIdList;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTaskMode() {
        return taskMode;
    }

    public void setTaskMode(int taskMode) {
        this.taskMode = taskMode;
    }

    public HumanStateChange getHumanStateChange() {
        return humanStateChange;
    }

    public void setHumanStateChange(HumanStateChange humanStateChange) {
        this.humanStateChange = humanStateChange;
    }

    public ResourceStateChange getResourceStateChange() {
        return resourceStateChange;
    }

    public void setResourceStateChange(ResourceStateChange resourceStateChange) {
        this.resourceStateChange = resourceStateChange;
    }

    public ProgressChange getProgressChange() {
        return progressChange;
    }

    public void setProgressChange(ProgressChange progressChange) {
        this.progressChange = progressChange;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public int getMovable() {
        return movable;
    }

    public void setMovable(int movable) {
        this.movable = movable;
    }

    public int getGravity() {
        return gravity;
    }

    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRownum() {
        return rownum;
    }

    public void setRownum(Integer rownum) {
        this.rownum = rownum;
    }

    public List<Integer> getDependencyIdList() {
        return dependencyIdList;
    }

    public void setDependencyIdList(List<Integer> dependencyIdList) {
        this.dependencyIdList = dependencyIdList;
    }

    public int getSplittable() {
        return splittable;
    }

    public void setSplittable(int splittable) {
        this.splittable = splittable;
    }

    public int getChangeable() {
        return changeable;
    }

    public void setChangeable(int changeable) {
        this.changeable = changeable;
    }
}

