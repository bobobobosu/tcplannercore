package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;

import static com.google.common.base.Preconditions.checkNotNull;

public class ChronoProperty extends AbstractPersistable {
    @Nullable
    private String startTime;
    @Nullable
    private String deadline;
    private Integer movable;
    private Integer splittable;
    private Integer changeable;
    private Integer gravity;

    @JsonIgnore
    private ZonedDateTime zonedStartTime;
    @JsonIgnore
    private ZonedDateTime zonedDeadline;

    public ChronoProperty() {
        super();
    }

    public ChronoProperty(ChronoProperty other) {
        super(other);
        this.setStartTime(other.startTime);
        this.setDeadline(other.deadline);
        this.setMovable(other.movable);
        this.setGravity(other.gravity);
        this.setSplittable(other.splittable);
        this.setChangeable(other.changeable);
    }


    @Override
    public ChronoProperty removeVolatile() {
        return this;
    }

    @Override
    public ChronoProperty removeEmpty() {
        return this;
    }

    @Override
    public boolean checkValid() {
        checkNotNull(movable);
        checkNotNull(splittable);
        checkNotNull(changeable);
        checkNotNull(gravity);
        return true;
    }

    @JsonIgnore
    public ZonedDateTime getZonedStartTime() {
        if (startTime == null) return null;
        if (zonedStartTime == null) this.zonedStartTime = ZonedDateTime.parse(startTime);
        return zonedStartTime;
    }

    @JsonIgnore
    public ZonedDateTime getZonedDeadline() {
        if (deadline == null) return null;
        if (zonedDeadline == null) this.zonedDeadline = ZonedDateTime.parse(deadline);
        return zonedDeadline;
    }

    public String getStartTime() {
        return startTime;
    }

    public ChronoProperty setStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    public String getDeadline() {
        return deadline;
    }

    public ChronoProperty setDeadline(String deadline) {
        this.deadline = deadline;
        return this;
    }

    public Integer getMovable() {
        return movable;
    }

    public ChronoProperty setMovable(Integer movable) {
        this.movable = movable;
        return this;
    }

    public Integer getSplittable() {
        return splittable;
    }

    public ChronoProperty setSplittable(Integer splittable) {
        this.splittable = splittable;
        return this;
    }

    public Integer getChangeable() {
        return changeable;
    }

    public ChronoProperty setChangeable(Integer changeable) {
        this.changeable = changeable;
        return this;
    }

    public Integer getGravity() {
        return gravity;
    }

    public ChronoProperty setGravity(Integer gravity) {
        this.gravity = gravity;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ChronoProperty that = (ChronoProperty) o;

        if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) return false;
        if (deadline != null ? !deadline.equals(that.deadline) : that.deadline != null) return false;
        if (!movable.equals(that.movable)) return false;
        if (!splittable.equals(that.splittable)) return false;
        if (!changeable.equals(that.changeable)) return false;
        return gravity.equals(that.gravity);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (deadline != null ? deadline.hashCode() : 0);
        result = 31 * result + movable.hashCode();
        result = 31 * result + splittable.hashCode();
        result = 31 * result + changeable.hashCode();
        result = 31 * result + gravity.hashCode();
        return result;
    }
}
