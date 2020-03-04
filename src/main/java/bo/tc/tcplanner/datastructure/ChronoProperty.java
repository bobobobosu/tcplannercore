package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.firebase.database.Exclude;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.Objects;

import static bo.tc.tcplanner.FunctionConstants.ZonedDateTimeParseCache;
import static com.google.common.base.Preconditions.checkNotNull;

public class ChronoProperty extends AbstractPersistable {
    @Nullable
    private String startTime;
    @Nullable
    private String deadline;
    @Nullable
    private String aliveline;
    private Integer movable;
    private Integer splittable;
    private Integer changeable;
    private Integer gravity;

    public ChronoProperty() {
        super();
    }

    public ChronoProperty(ChronoProperty other) {
        super(other);
        this.setStartTime(other.startTime);
        this.setDeadline(other.deadline);
        this.setAliveline(other.aliveline);
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
        try {
            checkNotNull(movable);
            checkNotNull(splittable);
            checkNotNull(changeable);
            checkNotNull(gravity);
            return true;
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(this.toString(), ex);
        }
    }

    @JsonIgnore
    @Exclude
    public ZonedDateTime getZonedStartTime() {
        if (startTime == null) return null;
        return ZonedDateTimeParseCache.computeIfAbsent(startTime, k -> ZonedDateTime.parse(startTime));
    }

    @JsonIgnore
    @Exclude
    public ZonedDateTime getZonedDeadline() {
        if (deadline == null) return null;
        return ZonedDateTimeParseCache.computeIfAbsent(deadline, k -> ZonedDateTime.parse(deadline));
    }

    @JsonIgnore
    @Exclude
    public ZonedDateTime getZonedAliveline() {
        if (deadline == null) return null;
        return ZonedDateTimeParseCache.computeIfAbsent(aliveline, k -> ZonedDateTime.parse(aliveline));
    }

    @Nullable
    public String getStartTime() {
        return startTime;
    }

    public ChronoProperty setStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    @Nullable
    public String getDeadline() {
        return deadline;
    }

    public ChronoProperty setDeadline(String deadline) {
        this.deadline = deadline;
        return this;
    }

    @Nullable
    public String getAliveline() {
        return aliveline;
    }

    public ChronoProperty setAliveline(String aliveline) {
        this.aliveline = aliveline;
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
        return movable.equals(that.movable) &&
                splittable.equals(that.splittable) &&
                changeable.equals(that.changeable) &&
                gravity.equals(that.gravity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), movable, splittable, changeable, gravity);
    }
}
