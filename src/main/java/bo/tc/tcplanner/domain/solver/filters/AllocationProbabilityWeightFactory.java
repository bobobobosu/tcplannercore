package bo.tc.tcplanner.domain.solver.filters;

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionProbabilityWeightFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.Iterator;

import static bo.tc.tcplanner.PropertyConstants.focused2dummyRatio;

//import static bo.tc.tcplanner.PropertyConstants.focused2dummyRatio;

public class AllocationProbabilityWeightFactory implements SelectionProbabilityWeightFactory<Schedule, Allocation> {

    @Override
    public double createProbabilityWeight(ScoreDirector<Schedule> scoreDirector, Allocation selection) {
        if (selection.isFocused()) {
            if (selection.getTimelineEntry().getTimelineProperty().getPlanningWindowType()
                    .equals(PropertyConstants.PlanningWindowTypes.types.Draft.name())) return focused2dummyRatio;
            return 1;
        }
        return dummyAccepted(selection) ? focused2dummyRatio : 0;
    }

    public static boolean dummyAccepted(Allocation selection) {
        Allocation prevAllocation = selection.getPrevAllocation();
        Allocation nextAllocation = selection.getNextAllocation();
        if (prevAllocation == null || nextAllocation == null) return true;
        int mid = (prevAllocation.getIndex() + nextAllocation.getIndex()) / 2;
        return selection.getIndex() == mid;
    }

    public static Iterator<Allocation> getCondensedAllocationIterator(Allocation allocation) {
        return new Iterator<Allocation>() {
            Allocation thisAllocation = null;

            @Override
            public boolean hasNext() {
                if (thisAllocation == null) return allocation != null;
                return thisAllocation.getNextAllocation() != null;
            }

            @Override
            public Allocation next() {
                if (thisAllocation == null) {
                    thisAllocation = allocation;
                } else {
                    Allocation nextAllocation = thisAllocation.getNextAllocation();
                    if (nextAllocation.getIndex() - thisAllocation.getIndex() == 1 || !thisAllocation.isFocused()) {
                        thisAllocation = nextAllocation;
                    } else {
                        thisAllocation = thisAllocation.getSchedule().getAllocationList().get(
                                (thisAllocation.getIndex() + nextAllocation.getIndex()) / 2);
                    }
                }
                return thisAllocation;
            }
        };
    }

    public static Iterator<Allocation> getDummyAllocationIterator(Allocation allocation) {
        return new Iterator<Allocation>() {
            Allocation thisAllocation = allocation;

            @Override
            public boolean hasNext() {
                Allocation nextAllocation = thisAllocation.getNextAllocation();
                while (nextAllocation != null && nextAllocation.getIndex() - thisAllocation.getIndex() == 1) {
                    thisAllocation = nextAllocation;
                    nextAllocation = nextAllocation.getNextAllocation();
                }
                return nextAllocation != null;
            }

            @Override
            public Allocation next() {
                Allocation nextAllocation = thisAllocation.getNextAllocation();
                while (nextAllocation.getIndex() - thisAllocation.getIndex() == 1) {
                    thisAllocation = nextAllocation;
                    nextAllocation = nextAllocation.getNextAllocation();
                }
                Allocation dummyAllocation = thisAllocation.getSchedule().getAllocationList().get((thisAllocation.getIndex() + nextAllocation.getIndex()) / 2);
                thisAllocation = nextAllocation;
                return dummyAllocation;
            }
        };
    }
}
