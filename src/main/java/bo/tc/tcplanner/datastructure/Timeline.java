package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;

import java.util.List;

public class Timeline extends AbstractPersistable {
    private List<TimelineEntry> timelineEntryList;


    @Override
    public AbstractPersistable removeVolatile() {
        return null;
    }

    @Override
    public AbstractPersistable removeEmpty() {
        return null;
    }

    @Override
    public boolean checkValid() {
        return false;
    }
}
