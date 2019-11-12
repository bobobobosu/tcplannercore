/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package bo.tc.tcplanner.domain.solver;

import bo.tc.tcplanner.datastructure.ResourceElement;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.ExecutionMode;
import bo.tc.tcplanner.domain.Schedule;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory;

import java.util.HashMap;
import java.util.Map;

public class ExecutionModeStrengthWeightFactory implements SelectionSorterWeightFactory<Schedule, ExecutionMode> {

    @Override
    public ExecutionModeStrengthWeight createSorterWeight(Schedule schedule, ExecutionMode executionMode) {
        Map<ResourceElement, Double> requirementTotalMap = new HashMap<>(
                executionMode.getResourceStateChange().getResourceChange().size());
        for (Map.Entry<String, ResourceElement> resource : executionMode.getResourceStateChange().getResourceChange().entrySet()) {
            requirementTotalMap.put(resource.getValue(), 0d);
        }


        for (Allocation allocation : schedule.getAllocationList()) {
            for (Map.Entry<String, ResourceElement> resource : executionMode.getResourceStateChange().getResourceChange().entrySet()) {
                Double total = requirementTotalMap.get(resource.getValue());
                if (total != null) {
                    total += resource.getValue().getAmt();
                    requirementTotalMap.put(resource.getValue(), total);
                }
            }
        }

        double requirementDesirability = 0.0;
        for (Map.Entry<String, ResourceElement> resource : executionMode.getResourceStateChange().getResourceChange().entrySet()) {
            double total = requirementTotalMap.get(resource);
            if (total > resource.getValue().getCapacity()) {
                requirementDesirability += total - resource.getValue().getCapacity();

            }

        }
        return new ExecutionModeStrengthWeight(executionMode, requirementDesirability);
    }

    public static class ExecutionModeStrengthWeight implements Comparable<ExecutionModeStrengthWeight> {

        private final ExecutionMode executionMode;
        private final double requirementDesirability;

        public ExecutionModeStrengthWeight(ExecutionMode executionMode, double requirementDesirability) {
            this.executionMode = executionMode;
            this.requirementDesirability = requirementDesirability;
        }

        @Override
        public int compareTo(ExecutionModeStrengthWeight other) {
            return new CompareToBuilder()
                    // The less requirementsWeight, the less desirable resources are used
                    .append(requirementDesirability, other.requirementDesirability)
                    .append(executionMode.getId(), other.executionMode.getId())
                    .toComparison();
        }

    }


}