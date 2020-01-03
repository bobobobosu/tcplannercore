package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimelineBlock extends AbstractPersistable {
    private List<TimelineEntry> timelineEntryList;
    private String blockStartTime;
    private String blockEndTime;
    private Integer blockStartRow;
    private Integer blockEndRow;
    private String blockScheduleAfter;
    @Nullable
    private String origin;
    @Nullable
    private String score;

    @JsonIgnore
    private ZonedDateTime zonedBlockStartTime;
    @JsonIgnore
    private ZonedDateTime zonedBlockEndTime;
    @JsonIgnore
    private ZonedDateTime zonedBlockScheduleAfter;


    public TimelineBlock() {
        super();
    }

    public TimelineBlock(TimelineBlock timelineBlock) {
        super(timelineBlock);
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


    @Override
    public boolean checkValid() {
        checkNotNull(blockStartTime);
        checkNotNull(blockStartTime != null);
        checkNotNull(blockEndTime != null);
        checkNotNull(blockStartRow != null);
        checkNotNull(blockEndRow != null);
        checkNotNull(blockScheduleAfter != null);
        checkNotNull(timelineEntryList != null);
        checkArgument(getZonedBlockEndTime().isAfter(getZonedBlockStartTime()));
        checkArgument(timelineEntryList.stream().allMatch(TimelineEntry::checkValid));
        return true;
    }

    @Override
    public TimelineBlock removeVolatile() {
        timelineEntryList.forEach(TimelineEntry::removeVolatile);
        return this;
    }

    @Override
    public TimelineBlock removeEmpty() {
        timelineEntryList.forEach(TimelineEntry::removeEmpty);
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TimelineBlock that = (TimelineBlock) o;

        if (!timelineEntryList.equals(that.timelineEntryList)) return false;
        if (!blockStartTime.equals(that.blockStartTime)) return false;
        if (!blockEndTime.equals(that.blockEndTime)) return false;
        if (!blockStartRow.equals(that.blockStartRow)) return false;
        if (!blockEndRow.equals(that.blockEndRow)) return false;
        if (!blockScheduleAfter.equals(that.blockScheduleAfter)) return false;
        return origin.equals(that.origin);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + timelineEntryList.hashCode();
        result = 31 * result + blockStartTime.hashCode();
        result = 31 * result + blockEndTime.hashCode();
        result = 31 * result + blockStartRow.hashCode();
        result = 31 * result + blockEndRow.hashCode();
        result = 31 * result + blockScheduleAfter.hashCode();
        result = 31 * result + origin.hashCode();
        return result;
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
        return this;
    }

    public ZonedDateTime getZonedBlockStartTime() {
        if (zonedBlockStartTime == null) zonedBlockStartTime = ZonedDateTime.parse(blockStartTime);
        return zonedBlockStartTime;
    }

    public String getBlockEndTime() {
        return blockEndTime;
    }

    public TimelineBlock setBlockEndTime(String blockEndTime) {
        this.blockEndTime = blockEndTime;
        return this;
    }

    public ZonedDateTime getZonedBlockEndTime() {
        if (zonedBlockEndTime == null) zonedBlockEndTime = ZonedDateTime.parse(blockEndTime);
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

    public String getBlockScheduleAfter() {
        return blockScheduleAfter;
    }

    public TimelineBlock setBlockScheduleAfter(String blockScheduleAfter) {
        this.blockScheduleAfter = blockScheduleAfter;
        return this;
    }

    public ZonedDateTime getZonedBlockScheduleAfter() {
        zonedBlockScheduleAfter = ZonedDateTime.parse(blockScheduleAfter);
        return zonedBlockScheduleAfter;
    }


}
