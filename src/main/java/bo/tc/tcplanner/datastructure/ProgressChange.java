package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProgressChange extends AbstractPersistable {
    //percentage change
    double progressDelta;

    public ProgressChange(ProgressChange other) {
        super(other);
        this.progressDelta = other.progressDelta;
    }

    public ProgressChange() {
        super();
    }

    @Override
    public ProgressChange removeVolatile() {
        return this;
    }

    public double getProgressDelta() {
        return progressDelta;
    }

    public ProgressChange setProgressDelta(double progressDelta) {
        this.progressDelta = progressDelta;
        return this;
    }
}