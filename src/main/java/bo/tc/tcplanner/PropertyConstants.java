package bo.tc.tcplanner;

import bo.tc.tcplanner.datastructure.LocationHierarchyMap;
import bo.tc.tcplanner.datastructure.TimeHierarchyMap;
import bo.tc.tcplanner.datastructure.ValueEntryMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import static bo.tc.tcplanner.app.TCSchedulingApp.*;
import static bo.tc.tcplanner.app.Toolbox.*;

public class PropertyConstants {
    // paths
    public static String fpath_Constants = "S:\\root\\Constants.json";
    public static String path_Notebook;
    public static String fpath_TimelineBlock;
    public static String fpath_ValueEntryMap;
    public static String fpath_LocationHierarchyMap;
    public static String fpath_TimeHierarchyMap;
    public static String fpath_ScheduleSolution = "Schedule.xml";

    // solver
    public static int focused2dummyRatio = 25;
    public static int dummyCountBetween = 30;

    // dummyLocation, dummyTime
    public static String dummyLocation = "Undefined";
    public static String dummyTime = "Anytime";
    // Planning Constants
    public static double resourceIgnoreAmt = 0.001;

    // planningWindowType
    public static class PlanningWindowTypes {
        public static boolean isValid(String s) {
            return Arrays.stream(types.values()).map(Enum::name).collect(Collectors.toList()).contains(s);
        }

        public enum types {
            History,
            Published,
            Draft,
            Unplanned,
            Deleted
        }
    }

    // Resource State Change Type
    public static class ResourceStateChangeTypes {
        public static boolean isValid(String s) {
            return Arrays.stream(ResourceStateChangeTypes.types.values()).map(Enum::name).collect(Collectors.toList()).contains(s);
        }

        public enum types {
            delta,
            absolute,
            absolute_full
        }
    }

    // Solver Phases
    public enum SolverPhase {
        CH,
        FAST,
        ACCURATE,
        REDUCE
    }


    public static void setConstants() throws IOException {
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
