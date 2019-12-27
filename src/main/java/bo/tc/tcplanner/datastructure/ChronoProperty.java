package bo.tc.tcplanner.datastructure;

public class ChronoProperty {
    private String startTime = null;
    private String deadline = null;
    private Integer movable = null;
    private Integer gravity = null;
    private Integer splittable = null;
    private Integer changeable = null;


    public ChronoProperty(){}

    public ChronoProperty(ChronoProperty other){
        this.setStartTime(other.startTime);
        this.setDeadline(other.deadline);
        this.setMovable(other.movable);
        this.setGravity(other.gravity);
        this.setSplittable(other.splittable);
        this.setChangeable(other.changeable);
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

}
