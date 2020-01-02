package bo.tc.tcplanner.datastructure;

import com.google.common.collect.RangeSet;

import java.time.ZonedDateTime;
import java.util.HashMap;

import static com.google.common.base.Preconditions.checkArgument;

public class TimeEntryMap extends HashMap<String, RangeSet<ZonedDateTime>> {
    public boolean checkValid() {
        checkArgument(this.entrySet().stream().allMatch(x -> x.getValue() != null));
        return true;
    }
}
