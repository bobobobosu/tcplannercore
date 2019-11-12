package bo.tc.tcplanner.app;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static bo.tc.tcplanner.app.TCSchedulingApp.locationHierarchyMap;
import static bo.tc.tcplanner.app.TCSchedulingApp.timeHierarchyMap;
import static bo.tc.tcplanner.app.Toolbox.castList;
import static bo.tc.tcplanner.app.Toolbox.castString;

public class DroolsTools {
    public static boolean timeRestrictionCheck(ZonedDateTime globalStartTime, int startDateMinuteOffset, int endDateMinuteOffset,
                                               String timeRestriction) {
        ZonedDateTime startDate = globalStartTime;
        ZonedDateTime endDate = globalStartTime.plusMinutes(endDateMinuteOffset);
        if (!timeHierarchyMap.containsKey(timeRestriction))
            return true;
        List<Object> timeIntervalList = castList(timeHierarchyMap.get(timeRestriction));
        for (Object thistimeInterval : timeIntervalList) {
            boolean requirementsMet = true;
            for (Object restriction : castList(thistimeInterval)) {
                if (restriction instanceof String) {
                    ZonedDateTime restrictedStartDate = ZonedDateTime.parse(castString(restriction).split("/")[0]);
                    ZonedDateTime restrictedEndDate = ZonedDateTime.parse(castString(restriction).split("/")[1]);
                    if (startDate.compareTo(restrictedStartDate) >= 0 && endDate.compareTo(restrictedEndDate) < 0) {
                        // meets requirements
                    } else {
                        requirementsMet = false;
                    }
                } else {
                    String mode = castString(castList(restriction).get(0));
                    if (mode.equals("year")) {

                    } else if (mode.equals("month")) {
                        List<Integer> availableMonths = (List<Integer>) (castList(restriction).get(1));
                        if (!availableMonths.contains(startDate.getMonth())) {
                            requirementsMet = false;
                        }
                    } else if (mode.equals("week")) {

                    } else if (mode.equals("day")) {

                    } else if (mode.equals("weekday")) {
                        List<Integer> availableWeekdays = (List<Integer>) (castList(restriction).get(1));
                        if (!availableWeekdays.contains(startDate.getDayOfWeek())) {
                            requirementsMet = false;
                        }
                    } else if (mode.equals("time")) {
                        boolean thisrequirementsMet = false;
                        for (String timerestricetion : (List<String>) (castList(restriction).get(1))) {
                            LocalTime reqstartTime = LocalTime.parse(timerestricetion.split("/")[0]);
                            LocalTime reqendTime = LocalTime.parse(timerestricetion.split("/")[1]);
                            if (startDate.toLocalTime().compareTo(reqstartTime) > 0
                                    && endDate.toLocalTime().compareTo(reqendTime) < 0) {
                                thisrequirementsMet = true;
                            }
                        }
                        requirementsMet = thisrequirementsMet;
                    } else {

                    }
                }
            }
            if (requirementsMet)
                return true;
        }
        return false;
    }

    public static boolean locationRestrictionCheck(String available, String requirement) {
        if (requirement.equals("Undefined")) return true;
        if (available.equals(requirement)) return true;
        List<Object> availableLocations = locationHierarchyMap.containsKey(available)
                ? castList(locationHierarchyMap.get(available))
                : new ArrayList<>();
        boolean result = availableLocations.contains(requirement);
        return availableLocations.contains(requirement);
    }

}
