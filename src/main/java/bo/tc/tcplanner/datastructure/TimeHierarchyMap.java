package bo.tc.tcplanner.datastructure;

import java.util.LinkedHashMap;

import static com.google.common.base.Preconditions.checkArgument;

public class TimeHierarchyMap extends LinkedHashMap<String, Object> {
    public boolean checkValid() {
        checkArgument(this.entrySet().stream().allMatch(x -> x.getValue() != null));
        return true;
    }
}
