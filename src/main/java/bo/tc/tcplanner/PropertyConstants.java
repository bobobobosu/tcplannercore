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

    // Resource State Change Type
    public static class ResourceStateChangeTypes {
        public enum types {
            delta,
            absolute,
            absolute_full
        }

        public static boolean isValid(String s) {
            return Arrays.stream(ResourceStateChangeTypes.types.values()).map(Enum::name).collect(Collectors.toList()).contains(s);
        }
    }

    // Planning Constants
    public static double resourceIgnoreAmt = 0.001;

}
