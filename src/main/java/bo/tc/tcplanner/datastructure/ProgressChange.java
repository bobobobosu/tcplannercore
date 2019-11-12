package bo.tc.tcplanner.datastructure;

public class ProgressChange {
    //percentage change
    double startPercent;
    double plannedEndPercent;
    double actualEndPercent;

    public ProgressChange() {

    }

    public ProgressChange(double startPercent, double plannedEndPercent, double actualEndPercent) {
        this.startPercent = startPercent;
        this.plannedEndPercent = plannedEndPercent;
        this.actualEndPercent = actualEndPercent;
    }

    public double getStartPercent() {
        return startPercent;
    }

    public void setStartPercent(double startPercent) {
        this.startPercent = startPercent;
    }

    public double getPlannedEndPercent() {
        return plannedEndPercent;
    }

    public void setPlannedEndPercent(double plannedEndPercent) {
        this.plannedEndPercent = plannedEndPercent;
    }

    public double getActualEndPercent() {
        return actualEndPercent;
    }

    public void setActualEndPercent(double actualEndPercent) {
        this.actualEndPercent = actualEndPercent;
    }
}