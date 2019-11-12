package bo.tc.tcplanner.datastructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimelineBlock {
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

    public void setTimelineEntryList(List<TimelineEntry> timelineEntryList) {
        this.timelineEntryList = timelineEntryList;
    }

    public String getBlockStartTime() {
        return blockStartTime;
    }

    public void setBlockStartTime(String blockStartTime) {
        this.blockStartTime = blockStartTime;
    }

    public String getBlockEndTime() {
        return blockEndTime;
    }

    public void setBlockEndTime(String blockEndTime) {
        this.blockEndTime = blockEndTime;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public Integer getBlockStartRow() {
        return blockStartRow;
    }

    public void setBlockStartRow(Integer blockStartRow) {
        this.blockStartRow = blockStartRow;
    }

    public Integer getBlockEndRow() {
        return blockEndRow;
    }

    public void setBlockEndRow(Integer blockEndRow) {
        this.blockEndRow = blockEndRow;
    }

    public Integer getBlockScheduleAfter() {
        return blockScheduleAfter;
    }

    public void setBlockScheduleAfter(Integer blockScheduleAfter) {
        this.blockScheduleAfter = blockScheduleAfter;
    }
}
