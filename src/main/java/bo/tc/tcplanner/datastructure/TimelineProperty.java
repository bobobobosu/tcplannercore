package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.persistable.AbstractPersistable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class TimelineProperty extends AbstractPersistable {
    private int rownum;
    private Set<Integer> dependencyIdList;
    private List<Integer> taskChainIdList;
    private String planningWindowType; // See PropertyConstants
    @Nullable
    private Integer timelineid;


    public TimelineProperty() {
        super();
    }

    @Override
    public TimelineProperty removeVolatile() {
        return this;
    }

    @Override
    public TimelineProperty removeEmpty() {
        return this;
    }

    @Override
    public boolean checkValid() {
        checkArgument(rownum >= 0);
        checkNotNull(dependencyIdList);
        checkNotNull(taskChainIdList);
        checkNotNull(planningWindowType);
        checkNotNull(timelineid);
        checkArgument(PropertyConstants.PlanningWindowTypes.isValid(planningWindowType));
        checkArgument(taskChainIdList.contains(timelineid));
        return true;
    }

    public TimelineProperty(TimelineProperty other) {
        super(other);
        this.setRownum(other.rownum);
        this.setTimelineid(other.timelineid);
        this.setDependencyIdList(new TreeSet<>(other.dependencyIdList));
        this.setPlanningWindowType(other.planningWindowType);
        this.setTaskChainIdList(new ArrayList<>(other.taskChainIdList));
    }

    public int getRownum() {
        return rownum;
    }

    public TimelineProperty setRownum(int rownum) {
        this.rownum = rownum;
        return this;
    }

    public Set<Integer> getDependencyIdList() {
        return dependencyIdList;
    }

    public TimelineProperty setDependencyIdList(Set<Integer> dependencyIdList) {
        this.dependencyIdList = dependencyIdList;
        return this;
    }

    public Integer getTimelineid() {
        return timelineid;
    }

    public TimelineProperty setTimelineid(Integer timelineid) {
        this.timelineid = timelineid;
        return this;
    }

    public String getPlanningWindowType() {
        return planningWindowType;
    }

    public TimelineProperty setPlanningWindowType(String planningWindowType) {
        this.planningWindowType = planningWindowType;
        return this;
    }


    public List<Integer> getTaskChainIdList() {
        return taskChainIdList;
    }

    public TimelineProperty setTaskChainIdList(List<Integer> taskChainIdList) {
        this.taskChainIdList = taskChainIdList;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TimelineProperty that = (TimelineProperty) o;

        if (rownum != that.rownum) return false;
        if (!dependencyIdList.equals(that.dependencyIdList)) return false;
        if (!taskChainIdList.equals(that.taskChainIdList)) return false;
        if (!planningWindowType.equals(that.planningWindowType)) return false;
        return timelineid != null ? timelineid.equals(that.timelineid) : that.timelineid == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + rownum;
        result = 31 * result + dependencyIdList.hashCode();
        result = 31 * result + taskChainIdList.hashCode();
        result = 31 * result + planningWindowType.hashCode();
        result = 31 * result + (timelineid != null ? timelineid.hashCode() : 0);
        return result;
    }
}
