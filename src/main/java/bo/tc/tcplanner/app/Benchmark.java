package bo.tc.tcplanner.app;

import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;

import java.io.IOException;


public class Benchmark {
    public static void main(String[] args) throws IOException {
        PlannerBenchmarkFactory plannerBenchmarkFactory = PlannerBenchmarkFactory.createFromFreemarkerXmlResource(
                "tcplannercoreBenchmarkConfig.xml.ftl");
//        BenchmarkAggregatorFrame.createAndDisplay(plannerBenchmarkFactory);
        PlannerBenchmark plannerBenchmark = plannerBenchmarkFactory.buildPlannerBenchmark();
        plannerBenchmark.benchmarkAndShowReportInBrowser();
    }
}
