package bo.tc.tcplanner.app;

import bo.tc.tcplanner.datastructure.LocationHierarchyMap;
import com.google.common.collect.Range;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bo.tc.tcplanner.app.TCSchedulingApp.locationHierarchyMap;
import static bo.tc.tcplanner.app.TCSchedulingApp.timeHierarchyMap;
import static bo.tc.tcplanner.app.Toolbox.castList;
import static bo.tc.tcplanner.app.Toolbox.castString;
import static bo.tc.tcplanner.datastructure.converters.DataStructureBuilder.dummyLocation;

public class DroolsTools {
    public static boolean timeRestrictionCheck(ZonedDateTime globalStartTime, int startDateMinuteOffset, int endDateMinuteOffset,
                                               String timeRestriction) {
        if (!timeHierarchyMap.containsKey(timeRestriction))
            return true;

        boolean match = castList(timeHierarchyMap.get(timeRestriction)).stream().anyMatch(or_intervals ->
                ((List<HashMap<String, ArrayList>>) or_intervals).stream().allMatch(in_intervals ->
                        checkConstraintDict(in_intervals, Range.closed(
                                globalStartTime.plusMinutes(startDateMinuteOffset),
                                globalStartTime.plusMinutes(endDateMinuteOffset)
                        ))));
        return match;
    }

    public static boolean locationRestrictionCheck(String available, String requirement) {
        if (requirement.equals(dummyLocation)) return true;
        return locationHierarchyMap.containsKey(available) ?
                locationHierarchyMap.get(available).contains(requirement) :
                available.equals(requirement);
    }


    static boolean checkConstraintDict(HashMap<String, ArrayList> constraintDict, Range<ZonedDateTime> timerange) {
        List<Range<ZonedDateTime>> daysInTimerange = getDatesOverRange(timerange);
        for (Map.Entry constraint : constraintDict.entrySet()) {
            if (constraint.getKey().equals("day")) {
                if (!daysInTimerange.stream().allMatch(thisDay -> castList(constraint.getValue()).contains(thisDay.lowerEndpoint().getDayOfMonth())))
                    return false;
            }
            if (constraint.getKey().equals("weekday")) {
                if (!daysInTimerange.stream().allMatch(thisDay -> castList(constraint.getValue()).contains(thisDay.lowerEndpoint().getDayOfWeek().getValue())))
                    return false;
            }
            if (constraint.getKey().equals("time")) {
                return daysInTimerange.stream().allMatch(zonedDateTimeRange -> castList(constraint.getValue()).stream().anyMatch(or_interval -> {
                            LocalTime reqstartTime = LocalTime.parse(castString(or_interval).split("/")[0]);
                            LocalTime reqendTime = LocalTime.parse(castString(or_interval).split("/")[1]);
                            return zonedDateTimeRange.lowerEndpoint().toLocalTime().compareTo(reqstartTime) >= 0 &&
                                    zonedDateTimeRange.upperEndpoint().toLocalTime().compareTo(reqendTime) <= 0;
                        })
                );

            }
            if (constraint.getKey().equals("iso8601")) {
                ZonedDateTime restrictedStartDate = ZonedDateTime.parse(castString(constraint.getValue()).split("/")[0]);
                ZonedDateTime restrictedEndDate = ZonedDateTime.parse(castString(constraint.getValue()).split("/")[1]);
                if (timerange.lowerEndpoint().compareTo(restrictedStartDate) < 0 ||
                        timerange.upperEndpoint().compareTo(restrictedEndDate) > 0) return false;
            }

        }
        return true;
    }

    private static List<Range<ZonedDateTime>> getDatesOverRange(Range<ZonedDateTime> timerange) {
        List<ZonedDateTime> dateList = new ArrayList<>();
        List<Range<ZonedDateTime>> dateRangeList = new ArrayList<>();
        ZonedDateTime start = timerange.lowerEndpoint();
        while (start.getYear() <= timerange.upperEndpoint().getYear() &&
                start.getDayOfYear() <= timerange.upperEndpoint().getDayOfYear()) {
            dateList.add(start);
            start = start.plusDays(1);
        }

        for (ZonedDateTime zonedDateTime : dateList) {
            dateRangeList.add(Range.closed(
                    sameDay(zonedDateTime, timerange.lowerEndpoint()) ? timerange.lowerEndpoint() : zonedDateTime.with(LocalTime.MIN),
                    sameDay(zonedDateTime, timerange.upperEndpoint()) ? timerange.upperEndpoint() : zonedDateTime.with(LocalTime.MAX)));
        }

        return dateRangeList;
    }

    private static boolean sameDay(ZonedDateTime z1, ZonedDateTime z2) {
        return (z1.getDayOfYear() == z2.getDayOfYear()) && (z1.getYear() == z2.getYear());
    }

}
