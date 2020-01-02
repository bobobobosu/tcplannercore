package bo.tc.tcplanner.app;

import bo.tc.tcplanner.datastructure.LocationHierarchyMap;
import bo.tc.tcplanner.datastructure.TimeHierarchyMap;
import bo.tc.tcplanner.datastructure.ValueEntryMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static bo.tc.tcplanner.app.TCSchedulingApp.*;
import static bo.tc.tcplanner.app.Toolbox.*;


public class Benchmark {
    public static String fpath_Constants = "C:\\_DATA\\_Storage\\_Sync\\Devices\\root\\Constants.json";
    public static String path_Notebook;
    public static String fpath_TimelineBlock;
    public static String fpath_ValueEntryMap;
    public static String fpath_LocationHierarchyMap;
    public static String fpath_TimeHierarchyMap;

    public static void main(String[] args) throws IOException {
        setConstants();
        initializeFiles();
        PlannerBenchmarkFactory plannerBenchmarkFactory = PlannerBenchmarkFactory.createFromFreemarkerXmlResource(
                "tcplannercoreBenchmarkConfig.xml.ftl");
//        BenchmarkAggregatorFrame.createAndDisplay(plannerBenchmarkFactory);
        PlannerBenchmark plannerBenchmark = plannerBenchmarkFactory.buildPlannerBenchmark();
        plannerBenchmark.benchmarkAndShowReportInBrowser();
    }

    static void setConstants() throws IOException {
        HashMap ConstantsJson = new ObjectMapper().readValue(
                IOUtils.toString(new FileInputStream(new File(fpath_Constants)), StandardCharsets.UTF_8), HashMap.class);
        path_Notebook = castString(castDict(castDict(ConstantsJson.get("Paths")).get("Folders")).get("Notebook"));
        fpath_TimelineBlock = genPathFromConstants("TimelineBlock.json", ConstantsJson);
        fpath_ValueEntryMap = genPathFromConstants("ValueEntryMap.json", ConstantsJson);
        fpath_LocationHierarchyMap = genPathFromConstants("LocationHierarchyMap.json", ConstantsJson);
        fpath_TimeHierarchyMap = genPathFromConstants("TimeHierarchyMap.json", ConstantsJson);
    }

    public static void initializeFiles() {
        // Load TimelineBlock & ValueEntryMap
        try {
            valueEntryMap = new ObjectMapper().readValue(
                    IOUtils.toString(new FileInputStream(new File(fpath_ValueEntryMap)), StandardCharsets.UTF_8), ValueEntryMap.class);
            locationHierarchyMap = new ObjectMapper().readValue(
                    IOUtils.toString(new FileInputStream(new File(fpath_LocationHierarchyMap)), StandardCharsets.UTF_8),
                    LocationHierarchyMap.class);
            timeHierarchyMap = new ObjectMapper().readValue(
                    IOUtils.toString(new FileInputStream(new File(fpath_TimeHierarchyMap)), StandardCharsets.UTF_8),
                    TimeHierarchyMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
