package bo.tc.tcplanner.domain.solver.meters;

import bo.tc.tcplanner.domain.Allocation;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;

public class AllocationNearbyDistanceMeter implements NearbyDistanceMeter<Allocation, Allocation> {
    @Override
    public double getNearbyDistance(Allocation o, Allocation allocation) {
        return Math.abs(o.getIndex() - allocation.getIndex());
    }
}
