package bo.tc.tcplanner.datastructure;

import java.util.HashMap;

public class ValueEntryMap extends HashMap<String, ValueEntry> {
    public ValueEntryMap() {
    }

    public ValueEntryMap(ValueEntryMap valueEntryMap) {
        valueEntryMap.forEach((k, v) -> this.put(k, new ValueEntry(v)));
    }

    public ValueEntryMap removeVolatile() {
        this.entrySet().removeIf(x -> x.getValue().isVolatileFlag());
        this.forEach((k, v) -> v.removeVolatile());
        return this;
    }

    public ValueEntryMap removeEmpty() {
        this.forEach((k, v) -> v.removeEmpty());
        return this;
    }
}
