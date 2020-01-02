package bo.tc.tcplanner.domain.solver.comparators;

import bo.tc.tcplanner.datastructure.TimelineEntry;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Comparator;

public class TimelineEntryStrengthComparator implements Comparator<TimelineEntry> {
    @Override
    public int compare(TimelineEntry o1, TimelineEntry o2) {
        return new CompareToBuilder()
                .append(o1.getResourceStateChange().getResourceChange().size(), o2.getResourceStateChange().getResourceChange().size())
                .append(!o1.getHumanStateChange().getCurrentLocation().equals(o1.getHumanStateChange().getMovetoLocation()),
                        !o2.getHumanStateChange().getCurrentLocation().equals(o2.getHumanStateChange().getMovetoLocation()))
                .append(o1.getId(), o2.getId())
                .toComparison();
    }
}
