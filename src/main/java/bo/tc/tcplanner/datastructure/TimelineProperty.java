package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;

import java.util.Set;
import java.util.TreeSet;

public class TimelineProperty extends AbstractPersistable {
    private Integer rownum = null;
    private Set<Integer> dependencyIdList = new TreeSet<>();
    private Integer timelineid = null;


    public TimelineProperty(){
    }

    @Override
    public TimelineProperty removeVolatile() {
        return this;
    }

    public TimelineProperty(TimelineProperty other){
        this.setRownum(other.rownum);
        this.setTimelineid(other.timelineid);
        this.setDependencyIdList(new TreeSet<>(other.dependencyIdList));
    }

    public Integer getRownum() {
        return rownum;
    }

    public TimelineProperty setRownum(Integer rownum) {
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
}
