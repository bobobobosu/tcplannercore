package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Timeline extends AbstractPersistable {
    private List<TimelineEntry> timelineEntryList;
    private String owner;


    @Override
    public Timeline removeVolatile() {
        timelineEntryList.forEach(TimelineEntry::removeVolatile);
        return this;
    }

    @Override
    public Timeline removeEmpty() {
        timelineEntryList.forEach(TimelineEntry::removeEmpty);
        return this;
    }

    @Override
    public boolean checkValid() {
        checkNotNull(timelineEntryList);
        checkNotNull(owner);
        checkArgument(timelineEntryList.stream().allMatch(TimelineEntry::checkValid));
        return true;
    }

    public List<TimelineEntry> getTimelineEntryList() {
        return timelineEntryList;
    }

    public void setTimelineEntryList(List<TimelineEntry> timelineEntryList) {
        this.timelineEntryList = timelineEntryList;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
