package bo.tc.tcplanner.datastructure;

public class HumanStateChange {
    //location change
    String currentLocation;
    String movetoLocation;
    //time change
    double duration;
    String requirementTimerange;

    public HumanStateChange(){

    }
    public HumanStateChange(String currentLocation, String movetoLocation, double duration){
        this.currentLocation = currentLocation;
        this.movetoLocation = movetoLocation;
        this.duration = duration;
    }

    public HumanStateChange(String currentLocation, String movetoLocation, double duration,String requirementTimerange){
        this.currentLocation = currentLocation;
        this.movetoLocation = movetoLocation;
        this.duration = duration;
        this.requirementTimerange = requirementTimerange;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public String getMovetoLocation() {
        return movetoLocation;
    }

    public void setMovetoLocation(String movetoLocation) {
        this.movetoLocation = movetoLocation;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public String getRequirementTimerange() {
        return requirementTimerange;
    }

    public void setRequirementTimerange(String requirementTimerange) {
        this.requirementTimerange = requirementTimerange;
    }
}
