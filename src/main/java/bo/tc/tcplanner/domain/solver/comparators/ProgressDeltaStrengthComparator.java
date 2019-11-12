package bo.tc.tcplanner.domain.solver.comparators;

import java.io.Serializable;
import java.util.Comparator;

public class ProgressDeltaStrengthComparator implements Comparator<Integer>, Serializable {
    @Override
    public int compare(Integer a, Integer b) {
        return a.compareTo(b);
    }

}
