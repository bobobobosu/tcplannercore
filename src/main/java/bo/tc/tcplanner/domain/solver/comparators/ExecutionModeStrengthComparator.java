package bo.tc.tcplanner.domain.solver.comparators;

import bo.tc.tcplanner.domain.ExecutionMode;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Comparator;

public class ExecutionModeStrengthComparator implements Comparator<ExecutionMode> {
    @Override
    public int compare(ExecutionMode o1, ExecutionMode o2) {
        return new CompareToBuilder()
                .append(o1.getResourceStateChange().getResourceChange().size(), o2.getResourceStateChange().getResourceChange().size())
                .append(!o1.getHumanStateChange().getCurrentLocation().equals(o1.getMovetoLocation()), !o2.getHumanStateChange().getCurrentLocation().equals(o2.getMovetoLocation())) // Descending (but this is debatable)
                .append(o1.getId(), o2.getId())
                .toComparison();
    }
}
