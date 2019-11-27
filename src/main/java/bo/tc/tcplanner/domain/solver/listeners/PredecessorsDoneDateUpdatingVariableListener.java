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
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import static bo.tc.tcplanner.datastructure.converters.DataStructureBuilder.dummyJob;
import static bo.tc.tcplanner.domain.solver.listeners.ListenerTools.*;

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
        // Update Planning Duration
        scoreDirector.beforeVariableChanged(originalAllocation, "plannedDuration");
        if (originalAllocation.getJob() == dummyJob) {
            originalAllocation.setPlannedDuration(null);
        } else {
            updatePlanningDuration(originalAllocation);
        }
        scoreDirector.beforeVariableChanged(originalAllocation, "plannedDuration");

        // Update PredecessorDoneDate
        if (originalAllocation.getJob() == dummyJob) {
            originalAllocation.setPredecessorsDoneDate(null);
        }
        NonDummyAllocationIterator nonDummyAllocationIterator;
        while (originalAllocation.getPredecessorsDoneDate() == null){
            originalAllocation = NonDummyAllocationIterator.getPrev(originalAllocation);
        }
        nonDummyAllocationIterator = new NonDummyAllocationIterator(originalAllocation);
        Allocation prevAllocation = nonDummyAllocationIterator.next();
        while (nonDummyAllocationIterator.hasNext()) {
            Allocation thisAllocation = nonDummyAllocationIterator.next();
            scoreDirector.beforeVariableChanged(thisAllocation, "resourceElementMap");
            updatePredecessorsDoneDate(thisAllocation, prevAllocation);
            scoreDirector.afterVariableChanged(thisAllocation, "resourceElementMap");
            prevAllocation = thisAllocation;
        }

    }

}
