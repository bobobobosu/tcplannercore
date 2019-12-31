package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceElement extends AbstractPersistable {
    //numeric property
    double amt;
    //if amt<0, location is requirement
    //if amt>0, location is availability
    String location;
    Set<Integer> priorityTimelineIdList = new TreeSet<>();
    @JsonIgnore
    Allocation sourceAllocation;


    public ResourceElement() {
    }

    public ResourceElement(double amt, String location) {
        this.amt = amt;
        this.location = location;
    }

    public ResourceElement(ResourceElement resourceElement) {
        super(resourceElement);
        this.amt = resourceElement.getAmt();
        this.location = resourceElement.getLocation();
        this.setSourceAllocation(resourceElement.sourceAllocation);
        this.setPriorityTimelineIdList(new TreeSet<>(resourceElement.priorityTimelineIdList));
    }

    public double getAmt() {
        return amt;
    }

    public ResourceElement setAmt(double amt) {
        this.amt = amt;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public ResourceElement setLocation(String location) {
        this.location = location;
        return this;
    }

    @Override
    public String toString() {
        return String.valueOf(amt);
    }

    @Override
    public ResourceElement setVolatileFlag(boolean volatileFlag) {
        super.setVolatileFlag(volatileFlag);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceElement that = (ResourceElement) o;
        return Double.compare(that.amt, amt) == 0 &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public Set<Integer> getPriorityTimelineIdList() {
        return priorityTimelineIdList;
    }

    public ResourceElement setPriorityTimelineIdList(Set<Integer> priorityTimelineIdList) {
        this.priorityTimelineIdList = priorityTimelineIdList;
        return this;
    }

    public Allocation getSourceAllocation() {
        return sourceAllocation;
    }

    public ResourceElement setSourceAllocation(Allocation sourceAllocation) {
        this.sourceAllocation = sourceAllocation;
        return this;
    }
}