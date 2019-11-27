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
                <bestScoreLimit>[0/0/0/0/0]hard/[-2147483648/-2147483648/-2147483648/-2147483648]soft</bestScoreLimit>
                <unimprovedSecondsSpentLimit>30</unimprovedSecondsSpentLimit>
<#--                <millisecondsSpentLimit>80000</millisecondsSpentLimit>-->

            </termination>
        </solver>

    {</inheritedSolverBenchmark>
    }

<#--    files-->
    <#list ['TimelineBlockHard'] as solution>
<#--    numbers-->
    <#list [240] as acceptedCountLimit>
<#--    <#list ['0.3'] as etabuRatio>-->
    <#list [1, 2] as lateAcceptanceSize>
<#--    algorithm-->
    <#list ['<lateAcceptanceSize>${lateAcceptanceSize}</lateAcceptanceSize>'] as lateAcceptance>
    <#list ['<entityTabuRatio>0.02</entityTabuRatio>'] as tabu>
    <#list ['<moveTabuSize>1</moveTabuSize>'] as mtabu>
    <#list ['<undoMoveTabuSize>5</undoMoveTabuSize>'] as umtabu>
    <#list ['TCRules_P1.drl'] as scoreDrl>
    <#list ['<constructionHeuristic>
                 <constructionHeuristicType>FIRST_FIT</constructionHeuristicType>
             </constructionHeuristic>'] as constructionHeuristic>
    <#list ['<finalistPodiumType>STRATEGIC_OSCILLATION_BY_LEVEL</finalistPodiumType>'] as finalistPodiumType>
    <#list ['NEVER'] as pickEarlyType>
    <#list [2] as ProbabilityWeight>
    <#list [2] as ProbabilityWeight2>
<#--    Moves-->
    <#list ['<filterClass>bo.tc.tcplanner.domain.solver.filters.NotDummyAllocationFilter</filterClass>'] as NotDummyFilter>
    <#list ['<filterClass>bo.tc.tcplanner.domain.solver.filters.IndexAllocationFilter</filterClass>'] as IndexFilter>
    <#list ['<swapMoveSelector>
                <fixedProbabilityWeight>${ProbabilityWeight2}</fixedProbabilityWeight>
                <filterClass>bo.tc.tcplanner.domain.solver.filters.DummySwapMoveFilter</filterClass>
                <variableNameInclude>executionMode</variableNameInclude>
                <variableNameInclude>progressdelta</variableNameInclude>
            </swapMoveSelector>'] as swapMove>
    <#list ['<cartesianProductMoveSelector>
                <fixedProbabilityWeight>${50-ProbabilityWeight2-ProbabilityWeight}</fixedProbabilityWeight>
                <ignoreEmptyChildIterators>true</ignoreEmptyChildIterators>
                <changeMoveSelector>
                    <entitySelector id="entitySelector">
                        ${IndexFilter}
                        <filterClass>bo.tc.tcplanner.domain.solver.filters.UnlockedAllocationFilter</filterClass>
                        <filterClass>bo.tc.tcplanner.domain.solver.filters.ChangeableAllocationFilter</filterClass>
                    </entitySelector>
                    <valueSelector variableName="executionMode"/>
                </changeMoveSelector>
                <changeMoveSelector>
                    <entitySelector mimicSelectorRef="entitySelector"/>
                    <valueSelector variableName="progressdelta"/>
                </changeMoveSelector>
            </cartesianProductMoveSelector>'] as cartesian>
    <#list ['<changeMoveSelector>
                <fixedProbabilityWeight>${ProbabilityWeight}</fixedProbabilityWeight>
                <entitySelector>
                    ${IndexFilter}
                    <filterClass>bo.tc.tcplanner.domain.solver.filters.UnlockedAllocationFilter</filterClass>
                    <filterClass>bo.tc.tcplanner.domain.solver.filters.MovableAllocationFilter</filterClass>
                    ${NotDummyFilter}
                </entitySelector>
                <valueSelector variableName="delay"/>
            </changeMoveSelector>'] as delay>
    <#list ['<changeMoveSelector>
                <fixedProbabilityWeight>${50-ProbabilityWeight2-ProbabilityWeight}</fixedProbabilityWeight>
                <entitySelector>
                    ${IndexFilter}
                    <filterClass>bo.tc.tcplanner.domain.solver.filters.UnlockedAllocationFilter</filterClass>
                    <filterClass>bo.tc.tcplanner.domain.solver.filters.ChangeableAllocationFilter</filterClass>
                </entitySelector>
                <valueSelector variableName="executionMode"/>
            </changeMoveSelector>'] as executionMode>
    <#list ['<changeMoveSelector>
                ${50-ProbabilityWeight2-ProbabilityWeight}
                <entitySelector>
                    ${IndexFilter}
                    <filterClass>bo.tc.tcplanner.domain.solver.filters.UnlockedAllocationFilter</filterClass>
                    ${NotDummyFilter}
                </entitySelector>
                <valueSelector variableName="progressdelta"/>
            </changeMoveSelector>'] as progressdelta>
    <#list ['${progressdelta}
            ${executionMode}
            ${delay}'] as fineMoves>

    <solverBenchmark>
        <name>a${acceptedCountLimit?index}d${scoreDrl?index}l${lateAcceptanceSize?index}t${tabu?index}s${solution?index}</name>
        <problemBenchmarks>
            <inputSolutionFile>C:/_DATA/_Storage/_Sync/Devices/root/Code/tcplannercore/src/main/resources/Solutions/${solution}.json</inputSolutionFile>
        </problemBenchmarks>
        <solver>
            <scoreDirectorFactory>
                <scoreDrl>${scoreDrl}</scoreDrl>
            </scoreDirectorFactory>
            <localSearch>
                <unionMoveSelector>
                    ${swapMove}
                    ${cartesian}
                    ${fineMoves}
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







</plannerBenchmark>