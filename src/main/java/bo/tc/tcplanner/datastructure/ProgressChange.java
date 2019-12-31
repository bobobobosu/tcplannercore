package bo.tc.tcplanner.datastructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProgressChange {
    //percentage change
    double progressDelta;

    public ProgressChange(ProgressChange other) {
        this.setProgressDelta(other.progressDelta);
    }

    public ProgressChange(double progressDelta) {
        this.setProgressDelta(progressDelta);
    }

    public ProgressChange() {
    }

    public double getProgressDelta() {
        return progressDelta;
    }

    public ProgressChange setProgressDelta(double progressDelta) {
        this.progressDelta = progressDelta;
        return this;
    }
}