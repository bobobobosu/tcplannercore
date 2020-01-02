package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class HumanStateChange extends AbstractPersistable {
    //location change
    String currentLocation;
    String movetoLocation;
    //time change
    double duration;
    String requirementTimerange;

    public HumanStateChange() {
        super();
    }

    @Override
    public HumanStateChange removeVolatile() {
        return this;
    }

    @Override
    public HumanStateChange removeEmpty() {
        return this;
    }

    @Override
    public boolean checkValid() {
        checkNotNull(currentLocation);
        checkNotNull(movetoLocation);
        checkArgument(duration >= 0);
        checkNotNull(requirementTimerange);
        return true;
    }

    public HumanStateChange(HumanStateChange other) {
        super(other);
        this.setCurrentLocation(other.currentLocation);
        this.setMovetoLocation(other.movetoLocation);
        this.setDuration(other.duration);
        this.setRequirementTimerange(other.requirementTimerange);
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public HumanStateChange setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
        return this;
    }

    public String getMovetoLocation() {
        return movetoLocation;
    }

    public HumanStateChange setMovetoLocation(String movetoLocation) {
        this.movetoLocation = movetoLocation;
        return this;
    }

    public double getDuration() {
        return duration;
    }

    public HumanStateChange setDuration(double duration) {
        this.duration = duration;
        return this;
    }

    public String getRequirementTimerange() {
        return requirementTimerange;
    }

    public HumanStateChange setRequirementTimerange(String requirementTimerange) {
        this.requirementTimerange = requirementTimerange;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        HumanStateChange that = (HumanStateChange) o;

        if (Double.compare(that.duration, duration) != 0) return false;
        if (!currentLocation.equals(that.currentLocation)) return false;
        if (!movetoLocation.equals(that.movetoLocation)) return false;
        return requirementTimerange.equals(that.requirementTimerange);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        result = 31 * result + currentLocation.hashCode();
        result = 31 * result + movetoLocation.hashCode();
        temp = Double.doubleToLongBits(duration);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + requirementTimerange.hashCode();
        return result;
    }
}
