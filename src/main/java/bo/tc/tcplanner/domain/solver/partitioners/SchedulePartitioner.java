package bo.tc.tcplanner.domain.solver.partitioners;

import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import com.google.common.collect.Lists;
import org.optaplanner.core.impl.partitionedsearch.partitioner.SolutionPartitioner;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.ArrayList;
import java.util.List;

public class SchedulePartitioner implements SolutionPartitioner<Schedule> {
    private int partCount = 4;

    @Override
    public List<Schedule> splitWorkingSolution(ScoreDirector<Schedule> scoreDirector, Integer runnablePartThreadLimit) {
        Schedule schedule = scoreDirector.getWorkingSolution();
        int allocationCnt = schedule.getAllocationList().size();
        List<Schedule> partList = new ArrayList<>(partCount);
        for (int i = 0; i < partCount; i++) {
            partList.add(new Schedule(schedule));
        }

        List<Allocation> tmpAllocationList = new ArrayList<>(schedule.getAllocationList());
        Allocation sinkAllocation = tmpAllocationList.remove(tmpAllocationList.size() - 1);
        Allocation sourceAllocation = tmpAllocationList.remove(0);
        List<List<Allocation>> partitioned
                = Lists.partition(tmpAllocationList, partCount - 1);

        for (int i = 0; i < partCount; i++) {
            partList.get(i).setAllocationList(partitioned.get(i));
        }

        return partList;
    }

    public int getPartCount() {
        return partCount;
    }

    public void setPartCount(int partCount) {
        this.partCount = partCount;
    }
}
