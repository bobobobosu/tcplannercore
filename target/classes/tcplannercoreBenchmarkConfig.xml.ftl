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
            <solutionClass>bo.tc.tcplanner.domain.Schedule</solutionClass>
            <entityClass>bo.tc.tcplanner.domain.Allocation</entityClass>

            <termination>
                <bestScoreLimit>[0/0/0/0/0]hard/[0/-2147483648/-2147483648/-2147483648]soft</bestScoreLimit>
<#--                <unimprovedSecondsSpentLimit>30</unimprovedSecondsSpentLimit>-->
                <millisecondsSpentLimit>60000</millisecondsSpentLimit>

            </termination>
        </solver>

    </inheritedSolverBenchmark>


<#--    files-->
    <#list ['TimelineBlockSolutionMixed'] as solution>
<#--    numbers-->
    <#list [240] as acceptedCountLimit>
<#--    <#list ['0.3'] as etabuRatio>-->
    <#list [1] as lateAcceptanceSize>
<#--    algorithm-->
    <#list ['<lateAcceptanceSize>${lateAcceptanceSize}</lateAcceptanceSize>'] as lateAcceptance>
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

    <#list [0.08] as delayWeight>
    <#list ['${(1-delayWeight)/2}'?number] as progressWeight>
    <#list ['${(1-delayWeight)/2}'?number] as timelineEntryWeight>

    <#list [0.10] as swapWeight>
    <#list [0.10] as fineWeightRest>
    <#list [0.80] as cartesianWeightRest>

    <#list ['${(1-swapWeight)*fineWeightRest}'?number] as fineWeight>
    <#list ['${(1-swapWeight)*(1-fineWeightRest)*cartesianWeightRest}'?number] as cartesianWeight>
    <#list ['${(1-swapWeight)*(1-fineWeightRest)*(1-cartesianWeightRest)}'?number] as mergesplitWeight>


<#--    Moves-->
    <#list ['<filterClass>bo.tc.tcplanner.domain.solver.filters.IsFocusedFilter</filterClass>'] as IsFocusedFilter>
    <#list ['<filterClass>bo.tc.tcplanner.domain.solver.filters.DelayCanChangeFilter</filterClass>'] as DelayCanChangeFilter>
    <#list ['<filterClass>bo.tc.tcplanner.domain.solver.filters.TimelineEntryCanChangeFilter</filterClass>'] as TimelineEntryCanChangeFilter>
    <#list ['<filterClass>bo.tc.tcplanner.domain.solver.filters.ProgressDeltaCanChangeFilter</filterClass>'] as ProgressDeltaCanChangeFilter>

<#--    Swap Moves-->
    <#list [
            '<moveListFactory>
                <fixedProbabilityWeight>${swapWeight}</fixedProbabilityWeight>
                <moveListFactoryClass>bo.tc.tcplanner.domain.solver.moves.SkippedSwapMoveFactory</moveListFactoryClass>
            </moveListFactory>'] as swapMove>

<#--    cartesian Product Moves-->
    <#list ['<cartesianProductMoveSelector>
                <fixedProbabilityWeight>${(progressWeight+timelineEntryWeight)*cartesianWeight}</fixedProbabilityWeight>
                <ignoreEmptyChildIterators>true</ignoreEmptyChildIterators>
                <changeMoveSelector>
                    <entitySelector id="entitySelector">
                        ${TimelineEntryCanChangeFilter}
                    </entitySelector>
                    <valueSelector variableName="timelineEntry"/>
                </changeMoveSelector>
                <changeMoveSelector>
                    <entitySelector mimicSelectorRef="entitySelector"/>
                    <valueSelector variableName="progressdelta"/>
                </changeMoveSelector>
            </cartesianProductMoveSelector>'] as cartesiantimelineEntry>
    <#list ['<cartesianProductMoveSelector>
                <fixedProbabilityWeight>${delayWeight*cartesianWeight}</fixedProbabilityWeight>
                <ignoreEmptyChildIterators>true</ignoreEmptyChildIterators>
                <changeMoveSelector>
                    <entitySelector>
                        ${IsFocusedFilter}
                        ${DelayCanChangeFilter}
                    </entitySelector>
                    <valueSelector variableName="delay"/>
                </changeMoveSelector>
                <changeMoveSelector>
                    <entitySelector>
                        ${IsFocusedFilter}
                        ${DelayCanChangeFilter}
                    </entitySelector>
                    <valueSelector variableName="delay"/>
                </changeMoveSelector>
            </cartesianProductMoveSelector>'] as cartesiandelay>


<#--    fine Moves-->
    <#list ['<moveListFactory>
                <fixedProbabilityWeight>${timelineEntryWeight*fineWeight}</fixedProbabilityWeight>
                <moveListFactoryClass>bo.tc.tcplanner.domain.solver.moves.PreciseTimeEntryMoveFactory</moveListFactoryClass>
            </moveListFactory>'] as timelineEntry>
<#--        ,-->
<#--        '<changeMoveSelector>-->
<#--        <fixedProbabilityWeight>${timelineEntryWeight*fineWeight}</fixedProbabilityWeight>-->
<#--        <entitySelector>-->
<#--            ${TimelineEntryCanChangeFilter}-->
<#--        </entitySelector>-->
<#--        <valueSelector variableName="timelineEntry"/>-->
<#--    </changeMoveSelector>'-->
    <#list ['<changeMoveSelector>
                <fixedProbabilityWeight>${progressWeight*fineWeight}</fixedProbabilityWeight>
                <entitySelector>
                    ${IsFocusedFilter}
                    ${ProgressDeltaCanChangeFilter}
                </entitySelector>
                <valueSelector variableName="progressdelta"/>
            </changeMoveSelector>'] as progressdelta>

    <#list ['<changeMoveSelector>
                <fixedProbabilityWeight>${delayWeight*fineWeight/2}</fixedProbabilityWeight>
                <entitySelector>
                    ${IsFocusedFilter}
                    ${DelayCanChangeFilter}
                </entitySelector>
                <valueSelector variableName="delay"/>
            </changeMoveSelector>'] as delay>
    <#list ['<moveListFactory>
                <fixedProbabilityWeight>${delayWeight*fineWeight/2}</fixedProbabilityWeight>
                <moveListFactoryClass>bo.tc.tcplanner.domain.solver.moves.PreciseDelayMoveFactory</moveListFactoryClass>
            </moveListFactory>'] as precisedelay>

<#--        mergesplit-->
    <#list ['<moveListFactory>
            <fixedProbabilityWeight>${mergesplitWeight/2}</fixedProbabilityWeight>
            <moveListFactoryClass>bo.tc.tcplanner.domain.solver.moves.SplitTimelineEntryFactory</moveListFactoryClass>
        </moveListFactory>'] as splitMove>
    <#list ['<moveListFactory>
            <fixedProbabilityWeight>${mergesplitWeight/2}</fixedProbabilityWeight>
            <moveListFactoryClass>bo.tc.tcplanner.domain.solver.moves.MergeTimelineEntryMoveFactory</moveListFactoryClass>
        </moveListFactory>'] as mergeMove>

    <#list ['${progressdelta}
            ${timelineEntry}
            ${delay}
            ${precisedelay}'] as fineMoves>
    <#list ['${cartesiantimelineEntry}
             ${cartesiandelay}'] as cartesianMoves>
    <#list ['${mergeMove}
            ${splitMove}'] as mergesplitMoves>

    <solverBenchmark>
        <name>a${scoreDrl?index}</name>
        <problemBenchmarks>
            <inputSolutionFile>C:/_DATA/_Storage/_Sync/Devices/root/Code/tcplannercore/src/main/resources/Solutions/${solution}.json</inputSolutionFile>
        </problemBenchmarks>
        <solver>
            <environmentMode>${envmode}</environmentMode>
            <scoreDirectorFactory>
                <scoreDrl>${scoreDrl}</scoreDrl>
            </scoreDirectorFactory>
            <localSearch>
                <unionMoveSelector>
<#--                    ${swapMove}-->
                    ${fineMoves}
                    ${cartesianMoves}
<#--                    ${mergesplitMoves}-->
                </unionMoveSelector>
                <#if lateAcceptance != "" || tabu != "" || mtabu != "" || umtabu != "">
                    <acceptor>
                        ${lateAcceptance}
<#--                        ${tabu}-->
                        ${mtabu}
                        ${umtabu}
                    </acceptor>
                </#if>
                <forager>
                    <acceptedCountLimit>${acceptedCountLimit}</acceptedCountLimit>
                    ${finalistPodiumType}
                    <pickEarlyType>${pickEarlyType}</pickEarlyType>
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