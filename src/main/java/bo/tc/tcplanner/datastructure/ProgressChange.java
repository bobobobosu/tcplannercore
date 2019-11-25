package bo.tc.tcplanner.datastructure;

public class ProgressChange {
    //percentage change
    double progressDelta;

    public ProgressChange() {

    }

    public ProgressChange(double progressDelta) {
        this.progressDelta = progressDelta;
     }

    public double getProgressDelta() {
        return progressDelta;
    }

    public void setProgressDelta(double progressDelta) {
        this.progressDelta = progressDelta;
    }
}