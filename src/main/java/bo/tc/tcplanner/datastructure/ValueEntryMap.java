package bo.tc.tcplanner.datastructure;

import java.util.HashMap;

public class ValueEntryMap extends HashMap<String, ValueEntry> {
    public ValueEntryMap(){}
    public ValueEntryMap(ValueEntryMap valueEntryMap) {
        this.putAll(valueEntryMap);
    }
}
