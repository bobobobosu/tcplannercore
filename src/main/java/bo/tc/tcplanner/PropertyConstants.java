package bo.tc.tcplanner;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PropertyConstants {
    // dummyLocation, dummyTime
    public static String dummyLocation = "Undefined";
    public static String dummyTime = "Anytime";

    // planningWindowType
    public static class PlanningWindowTypes {
        public enum types {
            History,
            Published,
            Draft,
            Unplanned,
            Deleted
        }

        public static boolean isValid(String s) {
            return Arrays.stream(types.values()).map(Enum::name).collect(Collectors.toList()).contains(s);
        }
    }

    // dummy property
    public static class ResourceStateChangeMode {
        public static String Absolute = "absolute";
        public static String Delta = "delta";
    }

}
