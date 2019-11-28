package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;

public class ProgressChange extends AbstractPersistable {
    //percentage change
    double progressDelta;

    public ProgressChange(double progressDelta) {
        this.progressDelta = progressDelta;
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