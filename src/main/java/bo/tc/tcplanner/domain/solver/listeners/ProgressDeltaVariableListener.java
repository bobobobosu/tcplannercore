package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.domain.Allocation;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class ProgressDeltaVariableListener implements VariableListener<Allocation> {
    @Override
    public void beforeEntityAdded(ScoreDirector scoreDirector, Allocation allocation) {

    }

    @Override
    public void afterEntityAdded(ScoreDirector scoreDirector, Allocation allocation) {
    }

    @Override
    public void beforeVariableChanged(ScoreDirector scoreDirector, Allocation allocation) {

    }

    @Override
    public void afterVariableChanged(ScoreDirector scoreDirector, Allocation allocation) {

        scoreDirector.beforeVariableChanged(allocation, "plannedDuration");
        allocation.setPlannedDuration(allocation.getExecutionMode().getTimeduration() * allocation.getProgressdelta() / 100);
        scoreDirector.afterVariableChanged(allocation, "plannedDuration");
        // if(allocation.getJob().getName() == "t.Formal Paper"){
        //     if(allocation.getProgressdelta()!=100){
        //         System.out.println(allocation.getExecutionMode().getTimeduration()+ " "+((double)allocation.getProgressdelta())/100 + " " + allocation.getPlannedDuration());
        //     }
        // }
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector scoreDirector, Allocation allocation) {

    }

    @Override
    public void afterEntityRemoved(ScoreDirector scoreDirector, Allocation allocation) {

    }
}
