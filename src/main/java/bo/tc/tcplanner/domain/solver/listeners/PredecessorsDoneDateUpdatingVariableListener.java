/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bo.tc.tcplanner.domain.solver.listeners;

import bo.tc.tcplanner.domain.Allocation;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.ArrayDeque;
import java.util.Queue;

public class PredecessorsDoneDateUpdatingVariableListener implements VariableListener<Allocation> {

    @Override
    public void beforeEntityAdded(ScoreDirector scoreDirector, Allocation allocation) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(ScoreDirector scoreDirector, Allocation allocation) {
        updateAllocation(scoreDirector, allocation);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector scoreDirector, Allocation allocation) {
        // Do nothing
    }

    @Override
    public void afterVariableChanged(ScoreDirector scoreDirector, Allocation allocation) {
        updateAllocation(scoreDirector, allocation);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector scoreDirector, Allocation allocation) {
        // Do nothing
    }

    @Override
    public void afterEntityRemoved(ScoreDirector scoreDirector, Allocation allocation) {
        // Do nothing
    }

    protected void updateAllocation(ScoreDirector scoreDirector, Allocation originalAllocation) {
        Queue<Allocation> uncheckedSuccessorQueue = new ArrayDeque<>();
        uncheckedSuccessorQueue.addAll(originalAllocation.getSuccessorAllocationList());
        while (!uncheckedSuccessorQueue.isEmpty()) {
            Allocation allocation = uncheckedSuccessorQueue.remove();
            boolean updated = updatePredecessorsDoneDate(scoreDirector, allocation);
            if (updated) {
                uncheckedSuccessorQueue.addAll(allocation.getSuccessorAllocationList());
            }
        }
    }

    /**
     * @param scoreDirector never null
     * @param allocation    never null
     * @return true if the startDate changed
     */
    protected boolean updatePredecessorsDoneDate(ScoreDirector scoreDirector, Allocation allocation) {
        Integer doneDate = 0;
        Allocation prevAllocation = allocation;
        // while (prevAllocation.getPredecessorAllocationList().size()>0){
        //     prevAllocation = prevAllocation.getPredecessorAllocationList().get(0);
        //     if(prevAllocation.getExecutionMode().getTimeduration() > 0){
        //         int endDate = prevAllocation.getEndDate();
        //         doneDate = Math.max(doneDate, endDate);
        //         break;
        //     }
        // }
        for (Allocation predecessorAllocation : allocation.getPredecessorAllocationList()) {
            int endDate = predecessorAllocation.getEndDate();
            doneDate = Math.max(doneDate, endDate);
        }
        scoreDirector.beforeVariableChanged(allocation, "predecessorsDoneDate");
        allocation.setPredecessorsDoneDate(doneDate);
        scoreDirector.afterVariableChanged(allocation, "predecessorsDoneDate");
        return true;
    }

}
