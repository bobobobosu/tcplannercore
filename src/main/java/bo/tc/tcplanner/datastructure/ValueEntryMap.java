package bo.tc.tcplanner.datastructure;

import java.util.HashMap;

public class ValueEntryMap extends HashMap<String, ValueEntry> {
    public ValueEntryMap() {
    }

    public ValueEntryMap(ValueEntryMap valueEntryMap) {
        valueEntryMap.forEach((k, v) -> this.put(k, new ValueEntry(v)));
    }
}
