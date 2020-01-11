package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.TreeSet;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceElement extends AbstractPersistable {
    //numeric property
    double amt;
    //if amt<0, location is requirement
    //if amt>0, location is availability
    String location;

    @Nullable
    @JsonIgnore
    Set<Integer> priorityTimelineIdList;
    @Nullable
    @JsonIgnore
    Set<Allocation> appliedTimelineIdList;


    public ResourceElement() {
        super();
    }


    public ResourceElement(ResourceElement resourceElement) {
        super(resourceElement);
        this.amt = resourceElement.getAmt();
        this.location = resourceElement.getLocation();
        if (resourceElement.priorityTimelineIdList != null)
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
    public ResourceElement removeVolatile() {
        if (appliedTimelineIdList != null) appliedTimelineIdList.removeIf(Allocation::isVolatileFlag);
        return this;
    }

    @Override
    public ResourceElement removeEmpty() {
        if (appliedTimelineIdList != null) appliedTimelineIdList.forEach(Allocation::removeEmpty);
        return this;
    }

    @Override
    public boolean checkValid() {
        checkNotNull(location);
        return true;
    }


    public Set<Integer> getPriorityTimelineIdList() {
        return priorityTimelineIdList;
    }

    public ResourceElement setPriorityTimelineIdList(Set<Integer> priorityTimelineIdList) {
        this.priorityTimelineIdList = priorityTimelineIdList;
        return this;
    }

    public Set<Allocation> getAppliedTimelineIdList() {
        return appliedTimelineIdList;
    }

    public ResourceElement setAppliedTimelineIdList(Set<Allocation> appliedTimelineIdList) {
        this.appliedTimelineIdList = appliedTimelineIdList;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceElement that = (ResourceElement) o;

        if (Double.compare(that.amt, amt) != 0) return false;
        return location.equals(that.location);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(amt);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + location.hashCode();
        return result;
    }
}