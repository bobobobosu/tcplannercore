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

import java.util.Iterator;

import static bo.tc.tcplanner.domain.solver.listeners.ListenerTools.updatePlanningDuration;
import static bo.tc.tcplanner.domain.solver.listeners.ListenerTools.updatePredecessorsDoneDate;

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
        // Update PredecessorDoneDate
        if (!originalAllocation.isFocused()) {
            scoreDirector.beforeVariableChanged(originalAllocation, "predecessorsDoneDate");
            originalAllocation.setPredecessorsDoneDate(null);
            scoreDirector.afterVariableChanged(originalAllocation, "predecessorsDoneDate");
        }

        Allocation startAllocation = originalAllocation.getFocusedAllocationSet().lower(originalAllocation);
        startAllocation = startAllocation == null ? originalAllocation : startAllocation;
        Iterator<Allocation> focusedAllocationIterator = originalAllocation.getFocusedAllocationSet()
                .tailSet(startAllocation).iterator();

        Allocation prevAllocation = focusedAllocationIterator.next();

        while (focusedAllocationIterator.hasNext()) {
            Allocation thisAllocation = focusedAllocationIterator.next();
            scoreDirector.beforeVariableChanged(thisAllocation, "predecessorsDoneDate");
            boolean changed = updatePredecessorsDoneDate(thisAllocation, prevAllocation);
            scoreDirector.afterVariableChanged(thisAllocation, "predecessorsDoneDate");
            prevAllocation = thisAllocation;

            if (!changed && thisAllocation.getIndex() > originalAllocation.getIndex()) break;
        }

    }

}
