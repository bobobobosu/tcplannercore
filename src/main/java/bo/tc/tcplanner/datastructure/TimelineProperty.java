package bo.tc.tcplanner.datastructure;

import java.util.ArrayList;
import java.util.List;

public class TimelineProperty {
    private Integer rownum = null;
    private List<Integer> dependencyIdList = new ArrayList<>();
    private Integer timelineid = null;


    public TimelineProperty(){}

    public TimelineProperty(TimelineProperty other){
        this.setRownum(other.rownum);
        this.setTimelineid(other.timelineid);
        this.setDependencyIdList(new ArrayList<>(other.dependencyIdList));
    }

    public Integer getRownum() {
        return rownum;
    }

    public TimelineProperty setRownum(Integer rownum) {
        this.rownum = rownum;
        return this;
    }

    public List<Integer> getDependencyIdList() {
        return dependencyIdList;
    }

    public TimelineProperty setDependencyIdList(List<Integer> dependencyIdList) {
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
