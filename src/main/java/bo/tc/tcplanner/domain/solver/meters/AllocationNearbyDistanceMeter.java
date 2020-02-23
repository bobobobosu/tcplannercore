package bo.tc.tcplanner.domain.solver.meters;

import bo.tc.tcplanner.datastructure.TimelineEntry;
import bo.tc.tcplanner.domain.Allocation;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;

import static bo.tc.tcplanner.app.DroolsTools.locationRestrictionCheck;

public class AllocationNearbyDistanceMeter implements NearbyDistanceMeter<Allocation, TimelineEntry> {
    @Override
    public double getNearbyDistance(Allocation origin, TimelineEntry destination) {
        if (origin.getPreviousStandstill() == null) return 0;
        return locationRestrictionCheck(origin.getPreviousStandstill(), destination.getHumanStateChange().getCurrentLocation()) ? 0 : 10000;
    }
}
