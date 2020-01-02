package bo.tc.tcplanner.domain.solver.moves;

import bo.tc.tcplanner.datastructure.TimelineEntry;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


public class SetValueMove extends AbstractMove<Schedule> {
    private List<Allocation> allocationList;
    private List<AllocationValues> allocationValuesList;
    private List<AllocationValues> oldallocationValuesList;

    public SetValueMove(Allocation allocation1, Integer progressdelta1, TimelineEntry executionMode1, Integer delay1) {
        this.allocationList = Arrays.asList(allocation1);
        this.allocationValuesList = Arrays.asList(new AllocationValues()
                .setProgressDelta(progressdelta1)
                .setExecutionMode(executionMode1)
                .setDelay(delay1));
        this.oldallocationValuesList = Arrays.asList(new AllocationValues()
                .setProgressDelta(allocation1.getProgressdelta())
                .setExecutionMode(allocation1.getTimelineEntry())
                .setDelay(allocation1.getDelay()));
    }

    public SetValueMove(Allocation allocation1, Integer progressdelta1, TimelineEntry executionMode1, Integer delay1,
                        Allocation allocation2, Integer progressdelta2, TimelineEntry executionMode2, Integer delay2) {
        this.allocationList = Arrays.asList(allocation1, allocation2);
        this.allocationValuesList = Arrays.asList(
                new AllocationValues()
                        .setProgressDelta(progressdelta1)
                        .setExecutionMode(executionMode1)
                        .setDelay(delay1),
                new AllocationValues()
                        .setProgressDelta(progressdelta2)
                        .setExecutionMode(executionMode2)
                        .setDelay(delay2));
        this.oldallocationValuesList = Arrays.asList(
                new AllocationValues()
                        .setProgressDelta(allocation1.getProgressdelta())
                        .setExecutionMode(allocation1.getTimelineEntry())
                        .setDelay(allocation1.getDelay()),
                new AllocationValues()
                        .setProgressDelta(allocation2.getProgressdelta())
                        .setExecutionMode(allocation2.getTimelineEntry())
                        .setDelay(allocation2.getDelay()));
    }

    public SetValueMove(List<Allocation> allocationList, List<AllocationValues> allocationValuesList) {
        this.allocationList = allocationList;
        this.allocationValuesList = allocationValuesList;
        this.oldallocationValuesList = allocationList.stream()
                .map(x -> new AllocationValues()
                        .setProgressDelta(x.getProgressdelta())
                        .setExecutionMode(x.getTimelineEntry())
                        .setDelay(x.getDelay())).collect(Collectors.toList());
    }

    @Override
    protected AbstractMove<Schedule> createUndoMove(ScoreDirector<Schedule> scoreDirector) {
        return new SetValueMove(allocationList, oldallocationValuesList);
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Schedule> scoreDirector) {
        for (int i = 0; i < allocationList.size(); i++) {
            Allocation allocation = allocationList.get(i);
            AllocationValues allocationValues = allocationValuesList.get(i);
            allocationValues.apply(allocation, scoreDirector);
        }

    }

    @Override
    public boolean isMoveDoable(ScoreDirector<Schedule> scoreDirector) {
        return true;
    }

    @Override
    public SetValueMove rebase(ScoreDirector<Schedule> destinationScoreDirector) {
        if (allocationList.size() == 1) {
            return new SetValueMove(
                    destinationScoreDirector.lookUpWorkingObject(allocationList.get(0)),
                    destinationScoreDirector.lookUpWorkingObject(allocationValuesList.get(0).getProgressDelta()),
                    destinationScoreDirector.lookUpWorkingObject(allocationValuesList.get(0).getExecutionMode()),
                    destinationScoreDirector.lookUpWorkingObject(allocationValuesList.get(0).getDelay()));
        }
        if (allocationList.size() == 2) {
            return new SetValueMove(
                    destinationScoreDirector.lookUpWorkingObject(allocationList.get(0)),
                    destinationScoreDirector.lookUpWorkingObject(allocationValuesList.get(0).getProgressDelta()),
                    destinationScoreDirector.lookUpWorkingObject(allocationValuesList.get(0).getExecutionMode()),
                    destinationScoreDirector.lookUpWorkingObject(allocationValuesList.get(0).getDelay()),
                    destinationScoreDirector.lookUpWorkingObject(allocationList.get(1)),
                    destinationScoreDirector.lookUpWorkingObject(allocationValuesList.get(1).getProgressDelta()),
                    destinationScoreDirector.lookUpWorkingObject(allocationValuesList.get(1).getExecutionMode()),
                    destinationScoreDirector.lookUpWorkingObject(allocationValuesList.get(1).getDelay()));
        }
        return new SetValueMove(
                destinationScoreDirector.lookUpWorkingObject(allocationList),
                destinationScoreDirector.lookUpWorkingObject(allocationValuesList));
    }


    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < allocationList.size(); i++) {
            Allocation allocation = allocationList.get(i);
            AllocationValues allocationValues = allocationValuesList.get(i);
            s.append(allocation).append(" {");
            if (allocationValues.getProgressDelta() != null)
                s.append(allocation.getProgressdelta()).append(" -> ").append(allocationValues.getProgressDelta()).append(", ");
            if (allocationValues.getExecutionMode() != null)
                s.append(allocation.getTimelineEntry()).append(" -> ").append(allocationValues.getExecutionMode()).append(", ");
            if (allocationValues.getDelay() != null)
                s.append(allocation.getDelay()).append(" -> ").append(allocationValues.getDelay()).append(", ");
            s.append("} ");
        }
        s.deleteCharAt(s.length()-1);
        return s.insert(0,"SetValueMove ").toString();
    }

    @Override
    public String getSimpleMoveTypeDescription() {
        return "SetValueMove(Allocation.progressDelta,Allocation.executionMode,Allocation.delay)";
    }

    @Override
    public Collection<? extends Object> getPlanningEntities() {
        return allocationList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof SetValueMove) {
            SetValueMove other = (SetValueMove) o;
            return new EqualsBuilder()
                    .append(allocationList, other.allocationList)
                    .append(allocationValuesList, other.allocationValuesList)
                    .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(allocationList)
                .append(allocationValuesList)
                .toHashCode();
    }

}
