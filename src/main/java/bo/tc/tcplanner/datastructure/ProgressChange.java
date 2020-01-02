package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import static com.google.common.base.Preconditions.checkArgument;

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

    @Override
    public ProgressChange removeEmpty() {
        return this;
    }

    @Override
    public boolean checkValid() {
        checkArgument(progressDelta >= 0);
        checkArgument(progressDelta <= 1);
        return true;
    }

    public double getProgressDelta() {
        return progressDelta;
    }

    public ProgressChange setProgressDelta(double progressDelta) {
        this.progressDelta = progressDelta;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ProgressChange that = (ProgressChange) o;

        return Double.compare(that.progressDelta, progressDelta) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(progressDelta);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}