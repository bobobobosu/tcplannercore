<?xml version="1.0" encoding="UTF-8"?>
<plannerBenchmark>
    <benchmarkDirectory>benchmark/tcplannercore</benchmarkDirectory>
<#--    <parallelBenchmarkCount>AUTO</parallelBenchmarkCount>-->
    <inheritedSolverBenchmark>
        <problemBenchmarks>
            <solutionFileIOClass>bo.tc.tcplanner.datastructure.persistence.ScheduleFileIO</solutionFileIOClass>
            <problemStatisticType>BEST_SCORE</problemStatisticType>
            <problemStatisticType>STEP_SCORE</problemStatisticType>
            <problemStatisticType>MEMORY_USE</problemStatisticType>
            <problemStatisticType>CALCULATE_COUNT_PER_SECOND</problemStatisticType>
            <singleStatisticType>CONSTRAINT_MATCH_TOTAL_BEST_SCORE</singleStatisticType>
            <singleStatisticType>PICKED_MOVE_TYPE_BEST_SCORE_DIFF</singleStatisticType>
        </problemBenchmarks>

        <solver>
            <moveThreadCount>AUTO</moveThreadCount>
<#--            <solutionClass>bo.tc.tcplanner.domain.Schedule</solutionClass>-->
<#--            <entityClass>bo.tc.tcplanner.domain.Allocation</entityClass>-->
            <scanAnnotatedClasses/>

            <termination>
                <bestScoreLimit>[0/0/0/0/0]hard/[-2147483648/-2147483648/-2147483648/-2147483648]soft</bestScoreLimit>
<#--                <unimprovedSecondsSpentLimit>30</unimprovedSecondsSpentLimit>-->
                <millisecondsSpentLimit>60000</millisecondsSpentLimit>

            </termination>
        </solver>

    </inheritedSolverBenchmark>


<#--    files-->
    <#list ['TimelineBlockProblem'] as solution>
<#--    numbers-->
    <#list [350] as acceptedCountLimit>
    <#list [10000] as startingTemperature>
<#--    <#list ['0.3'] as etabuRatio>-->
    <#list [1] as lateAcceptanceSize>
<#--    algorithm-->
    <#list ['<lateAcceptanceSize>${lateAcceptanceSize}</lateAcceptanceSize>'] as lateAcceptance>
    <#list ['<simulatedAnnealingStartingTemperature>[${startingTemperature}/${startingTemperature}/${startingTemperature}/${startingTemperature}/${startingTemperature}]hard/[0/0/0/0]soft</simulatedAnnealingStartingTemperature>'] as simulatedAnnealing>
    <#list ['<entityTabuRatio>0.02</entityTabuRatio>'] as tabu>
    <#list ['<moveTabuSize>1</moveTabuSize>'] as mtabu>
    <#list ['<undoMoveTabuSize>5</undoMoveTabuSize>'] as umtabu>
    <#list ['REPRODUCIBLE'] as envmode>
    <#list ['TCRules_P1.drl'] as scoreDrl>
    <#list ['<constructionHeuristic>
                 <constructionHeuristicType>FIRST_FIT</constructionHeuristicType>
             </constructionHeuristic>'] as constructionHeuristic>
    <#list ['<finalistPodiumType>STRATEGIC_OSCILLATION_BY_LEVEL</finalistPodiumType>'] as finalistPodiumType>
    <#list ['NEVER'] as pickEarlyType>
    <#list [''] as selectionOrderCacheType>
<#--     <#list ['<selectionOrder>ORIGINAL</selectionOrder>',''] as selectionOrderCacheType>-->
    <#list [''] as entitySelector>
<#--    <#list ['     <cacheType>PHASE</cacheType>-->
<#--                  <selectionOrder>SORTED</selectionOrder>-->
<#--                  <sorterManner>DECREASING_DIFFICULTY</sorterManner>',''] as entitySelector>-->
    <#list ['${lateAcceptance}'] as algorithm>

    <#list [0.08] as delayWeight>
    <#list ['${(1-delayWeight)/2}'?number] as progressWeight>
    <#list ['${(1-delayWeight)/2}'?number] as timelineEntryWeight>

    <#list [0.10] as swapWeight>
    <#list [0.10] as fineWeightRest>
    <#list [0.80] as cartesianWeightRest>

    <#list ['${(1-swapWeight)*fineWeightRest}'?number] as fineWeight>
    <#list ['${(1-swapWeight)*(1-fineWeightRest)*cartesianWeightRest}'?number] as cartesianWeight>
    <#list ['${(1-swapWeight)*(1-fineWeightRest)*(1-cartesianWeightRest)}'?number] as mergesplitWeight>
<#--    &lt;#&ndash;    Selectors&ndash;&gt;-->
<#--        <#list ['<cacheType>PHASE</cacheType>-->
<#--                <selectionOrder>PROBABILISTIC</selectionOrder>-->
<#--                <probabilityWeightFactoryClass>bo.tc.tcplanner.domain.solver.meters.AllocationProbabilityWeightFactory</probabilityWeightFactoryClass>',''] as WeightFactoryClass>-->
        <#list [''] as WeightFactoryClass>
<#--    Moves-->
    <#list ['<filterClass>bo.tc.tcplanner.domain.solver.filters.IsFocusedFilter</filterClass>'] as IsFocusedFilter>
    <#list ['<filterClass>bo.tc.tcplanner.domain.solver.filters.DelayCanChangeFilter</filterClass>'] as DelayCanChangeFilter>
    <#list ['<filterClass>bo.tc.tcplanner.domain.solver.filters.TimelineEntryCanChangeFilter</filterClass>'] as TimelineEntryCanChangeFilter>
    <#list ['<filterClass>bo.tc.tcplanner.domain.solver.filters.ProgressDeltaCanChangeFilter</filterClass>'] as ProgressDeltaCanChangeFilter>

    <#list [
        '<swapMoveSelector>
                <fixedProbabilityWeight>${swapWeight}</fixedProbabilityWeight>
                <entitySelector id="entitySelector2">
                    ${IsFocusedFilter}
                    ${TimelineEntryCanChangeFilter}
                </entitySelector>
                <secondaryEntitySelector>
                     ${TimelineEntryCanChangeFilter}
                </secondaryEntitySelector>
                <variableNameInclude>timelineEntry</variableNameInclude>
                <variableNameInclude>progressdelta</variableNameInclude>
            </swapMoveSelector>'] as swapMove>

<#--    cartesian Product Moves-->
    <#list ['<cartesianProductMoveSelector>
                <fixedProbabilityWeight>${(progressWeight+timelineEntryWeight)*cartesianWeight}</fixedProbabilityWeight>
                <ignoreEmptyChildIterators>true</ignoreEmptyChildIterators>
                <changeMoveSelector>
                    ${selectionOrderCacheType}
                    <entitySelector id="cartesianSelector1">
                        ${TimelineEntryCanChangeFilter}
                    </entitySelector>
                    <valueSelector>
                      <variableName>timelineEntry</variableName>
                      <nearbySelection>
                        <originEntitySelector mimicSelectorRef="cartesianSelector1"/>
                        <nearbyDistanceMeterClass>bo.tc.tcplanner.domain.solver.meters.AllocationNearbyDistanceMeter</nearbyDistanceMeterClass>
                        <nearbySelectionDistributionType>PARABOLIC_DISTRIBUTION</nearbySelectionDistributionType>
                      </nearbySelection>
                    </valueSelector>
                </changeMoveSelector>
                <changeMoveSelector>
                    <entitySelector mimicSelectorRef="cartesianSelector1"/>
                    <valueSelector variableName="progressdelta"/>
                </changeMoveSelector>
            </cartesianProductMoveSelector>'] as cartesiantimelineEntry>
    <#list ['<cartesianProductMoveSelector>
                <fixedProbabilityWeight>${delayWeight*cartesianWeight}</fixedProbabilityWeight>
                <ignoreEmptyChildIterators>true</ignoreEmptyChildIterators>
                <changeMoveSelector>
                    ${selectionOrderCacheType}
                    <entitySelector>
                        ${IsFocusedFilter}
                        ${DelayCanChangeFilter}
                    </entitySelector>
                    <valueSelector variableName="delay"/>
                </changeMoveSelector>
                <changeMoveSelector>
                    ${selectionOrderCacheType}
                    <entitySelector>
                        ${IsFocusedFilter}
                        ${DelayCanChangeFilter}
                    </entitySelector>
                    <valueSelector variableName="delay"/>
                </changeMoveSelector>
            </cartesianProductMoveSelector>'] as cartesiandelay>


<#--    fine Moves-->
    <#list ['<moveIteratorFactory>
                ${selectionOrderCacheType}
                <fixedProbabilityWeight>${timelineEntryWeight*fineWeight}</fixedProbabilityWeight>
                <moveIteratorFactoryClass>bo.tc.tcplanner.domain.solver.moves.PreciseTimeEntryMoveIteratorFactory</moveIteratorFactoryClass>
            </moveIteratorFactory>'] as timelineEntryCustom>
    <#list ['<changeMoveSelector>
                ${selectionOrderCacheType}
                <fixedProbabilityWeight>${timelineEntryWeight*fineWeight}</fixedProbabilityWeight>
                <entitySelector id="changeSelector1">
                    ${TimelineEntryCanChangeFilter}
                </entitySelector>
                <valueSelector>
                  <variableName>timelineEntry</variableName>
                  <nearbySelection>
                    <originEntitySelector mimicSelectorRef="changeSelector1"/>
                    <nearbyDistanceMeterClass>bo.tc.tcplanner.domain.solver.meters.AllocationNearbyDistanceMeter</nearbyDistanceMeterClass>
                    <nearbySelectionDistributionType>PARABOLIC_DISTRIBUTION</nearbySelectionDistributionType>
                  </nearbySelection>
                </valueSelector>
            </changeMoveSelector>'] as timelineEntry>

    <#list ['<changeMoveSelector>
                ${selectionOrderCacheType}
                <fixedProbabilityWeight>${progressWeight*fineWeight}</fixedProbabilityWeight>
                <entitySelector>
                    ${entitySelector}
                    ${IsFocusedFilter}
                    ${ProgressDeltaCanChangeFilter}
                </entitySelector>
                <valueSelector variableName="progressdelta"/>
            </changeMoveSelector>'] as progressdelta>

    <#list ['<changeMoveSelector>
                ${selectionOrderCacheType}
                <fixedProbabilityWeight>${delayWeight*fineWeight/2}</fixedProbabilityWeight>
                <entitySelector>
                    ${entitySelector}
                    ${IsFocusedFilter}
                    ${DelayCanChangeFilter}
                </entitySelector>
                <valueSelector variableName="delay"/>
            </changeMoveSelector>'] as delay>
    <#list ['<moveIteratorFactory>
                ${selectionOrderCacheType}
                <fixedProbabilityWeight>${delayWeight*fineWeight/2}</fixedProbabilityWeight>
                <moveIteratorFactoryClass>bo.tc.tcplanner.domain.solver.moves.PreciseDelayMoveIteratorFactory</moveIteratorFactoryClass>
            </moveIteratorFactory>'] as precisedelay>

<#--        mergesplit-->
    <#list ['<moveListFactory>
                ${selectionOrderCacheType}
                <fixedProbabilityWeight>${mergesplitWeight/2}</fixedProbabilityWeight>
                <moveListFactoryClass>bo.tc.tcplanner.domain.solver.moves.SplitTimelineEntryFactory</moveListFactoryClass>
            </moveListFactory>'] as splitMove>
    <#list ['<moveListFactory>
                ${selectionOrderCacheType}
                <fixedProbabilityWeight>${mergesplitWeight/2}</fixedProbabilityWeight>
                <moveListFactoryClass>bo.tc.tcplanner.domain.solver.moves.MergeTimelineEntryMoveFactory</moveListFactoryClass>
            </moveListFactory>'] as mergeMove>

    <#list ['${progressdelta}
            ${timelineEntry}
            ${precisedelay}
            ${delay}'] as fineMoves>
    <#list ['${cartesiantimelineEntry}
             ${cartesiandelay}'] as cartesianMoves>
    <#list ['${mergeMove}
            ${splitMove}'] as mergesplitMoves>

     <#list ['
                ${swapMove}
                ${progressdelta}
                ${timelineEntry}
                ${precisedelay}
                ${delay}
                ${cartesiantimelineEntry}
                ${cartesiandelay}
                ${mergeMove}
                ${splitMove}'] as customMoves>

    <solverBenchmark>
        <name>a${selectionOrderCacheType?index}</name>
        <problemBenchmarks>
            <inputSolutionFile>S:/root/Code/tcplannercore/src/main/resources/Solutions/${solution}.json</inputSolutionFile>
        </problemBenchmarks>
        <solver>
            <environmentMode>${envmode}</environmentMode>
            <scoreDirectorFactory>
                <scoreDrl>${scoreDrl}</scoreDrl>
            </scoreDirectorFactory>
            <localSearch>
                <unionMoveSelector>
                    ${customMoves}
                </unionMoveSelector>
                <#if lateAcceptance != "" || tabu != "" || mtabu != "" || umtabu != "">
                    <acceptor>
                            ${algorithm}
<#--                        ${lateAcceptance}-->
<#--                        ${tabu}-->
                        ${mtabu}
                        ${umtabu}
                    </acceptor>
                </#if>
                <forager>
<#--                    <acceptedCountLimit>4</acceptedCountLimit>-->
                                        <acceptedCountLimit>${acceptedCountLimit}</acceptedCountLimit>
                                        ${finalistPodiumType}
<#--                                        <pickEarlyType>${pickEarlyType}</pickEarlyType>-->
                </forager>
            </localSearch>
        </solver>
    </solverBenchmark>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
    </#list>
</plannerBenchmark>




<#--<#list ['<changeMoveSelector>-->
<#--                <fixedProbabilityWeight>${timelineEntryWeight*fineWeight}</fixedProbabilityWeight>-->
<#--                <entitySelector>-->
<#--                    ${IndexFilter}-->
<#--                    <filterClass>bo.tc.tcplanner.domain.solver.filters.UnlockedAllocationFilter</filterClass>-->
<#--                    <filterClass>bo.tc.tcplanner.domain.solver.filters.ChangeableAllocationFilter</filterClass>-->
<#--                </entitySelector>-->
<#--                <valueSelector variableName="timelineEntry"/>-->
<#--            </changeMoveSelector>'] as timelineEntry>-->

<#--<#list ['<cartesianProductMoveSelector>-->
<#--            <fixedProbabilityWeight>${delayWeight*cartesianWeight}</fixedProbabilityWeight>-->
<#--            <ignoreEmptyChildIterators>true</ignoreEmptyChildIterators>-->
<#--            <changeMoveSelector>-->
<#--                <entitySelector>-->
<#--                    ${IndexFilter}-->
<#--                    <filterClass>bo.tc.tcplanner.domain.solver.filters.UnlockedAllocationFilter</filterClass>-->
<#--                    <filterClass>bo.tc.tcplanner.domain.solver.filters.MovableAllocationFilter</filterClass>-->
<#--                    ${NotDummyFilter}-->
<#--                </entitySelector>-->
<#--                <valueSelector variableName="delay"/>-->
<#--            </changeMoveSelector>-->
<#--            <changeMoveSelector>-->
<#--                <entitySelector>-->
<#--                    ${IndexFilter}-->
<#--                    <filterClass>bo.tc.tcplanner.domain.solver.filters.UnlockedAllocationFilter</filterClass>-->
<#--                    <filterClass>bo.tc.tcplanner.domain.solver.filters.MovableAllocationFilter</filterClass>-->
<#--                    ${NotDummyFilter}-->
<#--                </entitySelector>-->
<#--                <valueSelector variableName="delay"/>-->
<#--            </changeMoveSelector>-->
<#--        </cartesianProductMoveSelector>'] as cartesiandelay>-->

<#--    Swap Moves-->
<#--    <#list ['<swapMoveSelector>-->
<#--                <fixedProbabilityWeight>${swapWeight}</fixedProbabilityWeight>-->
<#--                <entitySelector id="entitySelector2">-->
<#--                    ${TimelineEntryCanChangeFilter}-->
<#--                </entitySelector>-->
<#--                <secondaryEntitySelector>-->
<#--                      ${TimelineEntryCanChangeFilter}-->
<#--                    <nearbySelection>-->
<#--                        <originEntitySelector mimicSelectorRef="entitySelector2"/>-->
<#--                        <nearbyDistanceMeterClass>bo.tc.tcplanner.domain.solver.meters.AllocationNearbyDistanceMeter</nearbyDistanceMeterClass>-->
<#--                        <parabolicDistributionSizeMaximum>40</parabolicDistributionSizeMaximum>-->
<#--                    </nearbySelection>-->
<#--                </secondaryEntitySelector>-->
<#--                <variableNameInclude>timelineEntry</variableNameInclude>-->
<#--                <variableNameInclude>progressdelta</variableNameInclude>-->
<#--            </swapMoveSelector>'] as swapMove>-->

<#--            <#list [-->
<#--            '<moveListFactory>-->
<#--                <fixedProbabilityWeight>${swapWeight}</fixedProbabilityWeight>-->
<#--                <moveListFactoryClass>bo.tc.tcplanner.domain.solver.moves.SkippedSwapMoveFactory</moveListFactoryClass>-->
<#--            </moveListFactory>',-->
<#--            '<swapMoveSelector>-->
<#--                <fixedProbabilityWeight>${swapWeight}</fixedProbabilityWeight>-->
<#--                <entitySelector id="entitySelector2">-->
<#--                    ${TimelineEntryCanChangeFilter}-->
<#--                </entitySelector>-->
<#--                <secondaryEntitySelector>-->
<#--                      ${TimelineEntryCanChangeFilter}-->
<#--                    <nearbySelection>-->
<#--                        <originEntitySelector mimicSelectorRef="entitySelector2"/>-->
<#--                        <nearbyDistanceMeterClass>bo.tc.tcplanner.domain.solver.meters.AllocationNearbyDistanceMeter</nearbyDistanceMeterClass>-->
<#--                        <parabolicDistributionSizeMaximum>40</parabolicDistributionSizeMaximum>-->
<#--                    </nearbySelection>-->
<#--                </secondaryEntitySelector>-->
<#--                <variableNameInclude>timelineEntry</variableNameInclude>-->
<#--                <variableNameInclude>progressdelta</variableNameInclude>-->
<#--            </swapMoveSelector>',''] as swapMove>-->