package bo.tc.tcplanner.domain.solver.meters;

import bo.tc.tcplanner.datastructure.TimelineEntry;
import bo.tc.tcplanner.domain.Allocation;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;

import java.util.Set;

import static bo.tc.tcplanner.app.DroolsTools.locationRestrictionCheck;

public class AllocationNearbyDistanceMeter implements NearbyDistanceMeter<Allocation, TimelineEntry> {
    @Override
    public double getNearbyDistance(Allocation origin, TimelineEntry destination) {
//        Allocation nextAllocation = origin.getNextAllocation();
//        String des = destination.getHumanStateChange().getMovetoLocation();
//        String nex = nextAllocation.getTimelineEntry().getHumanStateChange().getCurrentLocation();
//        double res = locationRestrictionCheck(
//                destination.getHumanStateChange().getMovetoLocation(),
//                nextAllocation.getTimelineEntry().getHumanStateChange().getCurrentLocation()) ? 0 : 0;
//        if (res == 1) {
//            int g = 0;
//        }
        return 1;
    }
}
