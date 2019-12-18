package bo.tc.tcplanner.app;

import bo.tc.tcplanner.datastructure.LocationHierarchyMap;
import bo.tc.tcplanner.datastructure.ValueEntryMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Range;
import org.apache.commons.io.IOUtils;
import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bo.tc.tcplanner.app.TCSchedulingApp.*;
import static bo.tc.tcplanner.app.Toolbox.*;
import static bo.tc.tcplanner.app.Toolbox.genPathFromConstants;

public class Test {
    public static String fpath_Constants = "C:\\_DATA\\_Storage\\_Sync\\Devices\\root\\Constants.json";
    public static String path_Notebook;
    public static String fpath_TimelineBlock;
    public static String fpath_ValueEntryMap;
    public static String fpath_LocationHierarchyMap;
    public static String fpath_TimeHierarchyMap;

    public static void main(String[] args) throws IOException {
        setConstants();
        initializeFiles();
        HashMap<String, Object> gdfr = timeHierarchyMap;

        Range<ZonedDateTime> allRange = Range.closed(
                ZonedDateTime.parse("2019-01-14T00:00:00-07:00"),
                ZonedDateTime.parse("2019-12-15T00:00:00-07:00"));


        List<ZonedDateTime> totalDates = new ArrayList<>();
        ZonedDateTime start = allRange.lowerEndpoint();
        ZonedDateTime end = allRange.upperEndpoint();

        List<Object> wed = castList(timeHierarchyMap.get("Wednesday"));
        Range<ZonedDateTime> thisRange = Range.closed(
                ZonedDateTime.parse("2019-12-11T02:00:00-07:00"),
                ZonedDateTime.parse("2019-12-11T22:00:00-07:00"));

        boolean match = wed.stream().anyMatch(or_intervals ->
                ((List<HashMap<String, ArrayList>>) or_intervals).stream().allMatch(in_intervals -> checkConstraintDict(in_intervals, thisRange)));

        Range<ZonedDateTime> validGrades2 = Range.closed(
                ZonedDateTime.parse("2019-12-13T00:00:00-07:00"),
                ZonedDateTime.parse("2019-12-18T00:00:00-07:00"));

        int g = 0;
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
                for (Range<ZonedDateTime> zonedDateTimeRange : daysInTimerange) {
                    LocalTime reqstartTime = LocalTime.parse(castString(constraint.getValue()).split("/")[0]);
                    LocalTime reqendTime = LocalTime.parse(castString(constraint.getValue()).split("/")[1]);
                    if (zonedDateTimeRange.lowerEndpoint().toLocalTime().compareTo(reqstartTime) < 0 ||
                            zonedDateTimeRange.upperEndpoint().toLocalTime().compareTo(reqendTime) > 0) return false;
                }
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

    static List<Range<ZonedDateTime>> getDatesOverRange(Range<ZonedDateTime> timerange) {
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

    static boolean sameDay(ZonedDateTime z1, ZonedDateTime z2) {
        return (z1.getDayOfYear() == z2.getDayOfYear()) && (z1.getYear() == z2.getYear());
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
            timeHierarchyMap = (HashMap<String, Object>) (new ObjectMapper().readValue("{\"root\":"
                    + IOUtils.toString(new FileInputStream(new File(fpath_TimeHierarchyMap)), StandardCharsets.UTF_8) + "}", Map.class)
                    .get("root"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
