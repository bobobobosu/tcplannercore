package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.ZonedDateTime;

public class ChronoProperty extends AbstractPersistable {
    private String startTime = null;
    private String deadline = null;
    private Integer movable = null;
    private Integer gravity = null;
    private Integer splittable = null;
    private Integer changeable = null;

    @JsonIgnore
    private ZonedDateTime zonedStartTime;
    @JsonIgnore
    private ZonedDateTime zonedDeadline;

    public ChronoProperty() {
        super();
    }

    @Override
    public ChronoProperty removeVolatile() {
        return this;
    }

    @Override
    public ChronoProperty removeEmpty() {
        return this;
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


    @JsonIgnore
    public ZonedDateTime getZonedStartTime() {
        return zonedStartTime;
    }

    @JsonIgnore
    public ZonedDateTime getZonedDeadline() {
        return zonedDeadline;
    }

    public String getStartTime() {
        return startTime;
    }

    public ChronoProperty setStartTime(String startTime) {
        this.startTime = startTime;
        if (startTime != null) this.zonedStartTime = ZonedDateTime.parse(startTime);
        return this;
    }

    public String getDeadline() {
        return deadline;
    }

    public ChronoProperty setDeadline(String deadline) {
        this.deadline = deadline;
        if (deadline != null) this.zonedDeadline = ZonedDateTime.parse(deadline);
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

}
