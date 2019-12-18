package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceElement extends AbstractPersistable {
    //numeric property
    double amt;
    //if amt<0, location is requirement
    //if amt>0, location is availability
    String location;

    public ResourceElement() {
    }

    public ResourceElement(double amt, String location) {
        this.amt = amt;
        this.location = location;
    }

    public ResourceElement(ResourceElement resourceElement) {
        this.amt = resourceElement.getAmt();
        this.location = resourceElement.getLocation();
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
}