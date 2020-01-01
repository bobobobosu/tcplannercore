package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimelineBlock extends AbstractPersistable {
    private List<TimelineEntry> timelineEntryList;
    private String blockStartTime;
    private String blockEndTime;
    private Integer blockStartRow;
    private Integer blockEndRow;
    private Integer blockScheduleAfter;
    private String origin;
    private String score;

    @JsonIgnore
    private ZonedDateTime zonedBlockStartTime;
    @JsonIgnore
    private ZonedDateTime zonedBlockEndTime;


    public TimelineBlock() {
        super();
    }

    public TimelineBlock(TimelineBlock timelineBlock) {
        super(timelineBlock);
        if (timelineBlock.timelineEntryList != null)
            this.timelineEntryList = timelineBlock.timelineEntryList.stream().map(x -> new TimelineEntry(x))
                    .collect(Collectors.toList());
        this.setBlockStartTime(timelineBlock.blockStartTime);
        this.setBlockEndTime(timelineBlock.blockEndTime);
        this.blockStartRow = timelineBlock.blockStartRow;
        this.blockEndRow = timelineBlock.blockEndRow;
        this.blockScheduleAfter = timelineBlock.blockScheduleAfter;
        this.origin = timelineBlock.origin;
        this.score = timelineBlock.score;
    }

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
        if (blockStartTime != null) zonedBlockStartTime = ZonedDateTime.parse(blockStartTime);
        return this;
    }

    public ZonedDateTime getZonedBlockStartTime() {
        return zonedBlockStartTime;
    }

    public String getBlockEndTime() {
        return blockEndTime;
    }

    public TimelineBlock setBlockEndTime(String blockEndTime) {
        this.blockEndTime = blockEndTime;
        if (blockEndTime != null) zonedBlockEndTime = ZonedDateTime.parse(blockEndTime);
        return this;
    }

    public ZonedDateTime getZonedBlockEndTime() {
        return zonedBlockEndTime;
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

    @Override
    public TimelineBlock removeVolatile() {
        timelineEntryList.forEach(AbstractPersistable::removeVolatile);
        return this;
    }
}
