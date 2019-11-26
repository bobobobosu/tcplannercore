package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;

public class HumanStateChange extends AbstractPersistable {
    //location change
    String currentLocation;
    String movetoLocation;
    //time change
    double duration;
    String requirementTimerange;

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
}
