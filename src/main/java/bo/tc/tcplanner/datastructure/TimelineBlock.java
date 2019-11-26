package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimelineBlock extends AbstractPersistable {
    List<TimelineEntry> timelineEntryList;
    String blockStartTime;
    String blockEndTime;
    Integer blockStartRow;
    Integer blockEndRow;
    Integer blockScheduleAfter;
    String origin;
    String score;


    public List<TimelineEntry> getTimelineEntryList() {
        return timelineEntryList;
    }

    public TimelineBlock setTimelineEntryList(List<TimelineEntry> timelineEntryList) {
        this.timelineEntryList = timelineEntryList;
        return this;
    }

    public String getBlockStartTime() {
        return blockStartTime;
    }

    public TimelineBlock setBlockStartTime(String blockStartTime) {
        this.blockStartTime = blockStartTime;
        return this;
    }

    public String getBlockEndTime() {
        return blockEndTime;
    }

    public TimelineBlock setBlockEndTime(String blockEndTime) {
        this.blockEndTime = blockEndTime;
        return this;
    }

    public String getOrigin() {
        return origin;
    }

    public TimelineBlock setOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    public String getScore() {
        return score;
    }

    public TimelineBlock setScore(String score) {
        this.score = score;
        return this;
    }

    public Integer getBlockStartRow() {
        return blockStartRow;
    }

    public TimelineBlock setBlockStartRow(Integer blockStartRow) {
        this.blockStartRow = blockStartRow;
        return this;
    }

    public Integer getBlockEndRow() {
        return blockEndRow;
    }

    public TimelineBlock setBlockEndRow(Integer blockEndRow) {
        this.blockEndRow = blockEndRow;
        return this;
    }

    public Integer getBlockScheduleAfter() {
        return blockScheduleAfter;
    }

    public TimelineBlock setBlockScheduleAfter(Integer blockScheduleAfter) {
        this.blockScheduleAfter = blockScheduleAfter;
        return this;
    }
}
