package bo.tc.tcplanner.datastructure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

public class LocationHierarchyMap extends HashMap<String, HashSet<String>> {
    public boolean checkValid() {
        checkArgument(this.entrySet().stream().allMatch(x -> x.getValue().stream().allMatch(Objects::nonNull)));
        return true;
    }
}
