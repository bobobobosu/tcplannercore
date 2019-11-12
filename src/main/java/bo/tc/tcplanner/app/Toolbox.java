package bo.tc.tcplanner.app;

import bo.tc.tcplanner.datastructure.TimelineBlock;
import bo.tc.tcplanner.datastructure.TimelineEntry;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.core.api.solver.Solver;

import java.awt.*;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Toolbox {
    public static Integer ZonedDatetime2OffsetMinutes(ZonedDateTime startDatetime, ZonedDateTime targetedDatetime) {
        Duration duration = Duration.between(startDatetime, targetedDatetime);
        return Math.toIntExact(duration.toMinutes());
    }

    public static ZonedDateTime OffsetMinutes2ZonedDatetime(ZonedDateTime startDatetime, Integer offsetMinutes) {
        return startDatetime.plusMinutes(offsetMinutes);
    }

    public static LinkedHashMap<String, Object> castDict(Object obj) {
        return (LinkedHashMap<String, Object>) obj;
    }

    public static List<Object> castList(Object obj) {
        return (List<Object>) obj;
    }

    public static String castString(Object obj) {
        return (String) obj;
    }

    public static int castInt(Object obj) {
        return ((Number) obj).intValue();
    }

    public static long castLong(Object obj) {
        return ((Number) obj).longValue();
    }

    public static double castDouble(Object obj) {
        return ((Number) obj).doubleValue();
    }

    public static boolean castBoolean(Object obj) {
        return (boolean) obj;
    }

    public static void displayTray(String caption, String text) {
        //Obtain only one instance of the SystemTray object
        SystemTray tray = SystemTray.getSystemTray();

        //If the icon is a file
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
        //Alternative (if the icon is on the classpath):
        //Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("icon.png"));

        TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
        //Let the system resize the image if needed
        trayIcon.setImageAutoSize(true);
        //Set tooltip text for the tray icon
        trayIcon.setToolTip("System tray icon demo");
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }

        trayIcon.displayMessage(caption, text, TrayIcon.MessageType.INFO);
    }

    public static void terminateSolvers(List<Solver<Schedule>> solverList) {
        for (Solver solver : solverList) solver.terminateEarly();
        solverList = new ArrayList<>();
    }

    public static void printTimelineBlock(TimelineBlock newTimelineBlock) {
        // Print Results
        for (TimelineEntry timelineEntry : newTimelineBlock.getTimelineEntryList()) {
            String mandNote = (timelineEntry.getId() > 0) ? String.valueOf(timelineEntry.getRownum()) : "****";
            System.err.println(mandNote + " " + timelineEntry.getStartTime() + "~"
                    + ZonedDateTime.parse(timelineEntry.getStartTime())
                    .plusMinutes((long) timelineEntry.getHumanStateChange().getDuration())
                    + " : [" + timelineEntry.getTitle() + " ] "
                    + timelineEntry.getHumanStateChange().getCurrentLocation() + "->"
                    + timelineEntry.getHumanStateChange().getMovetoLocation());
        }
    }

    public static void printSchedule(Schedule schedule) {
        for (Allocation allocation : schedule.getAllocationList()) {
            System.out.println(allocation.getJob().getTimelineid() + " " +
                    allocation.getJob().getName() + " " +
                    allocation.getPlannedDuration() + " " +
                    allocation.getExecutionMode().getTimeduration() + " " +
                    allocation.getPredecessorsDoneDate() + " " +
                    allocation.getStartDate() + " " +
                    allocation.getEndDate() + " " +
                    allocation.getPreviousStandstill() + " " +
                    allocation.getExecutionMode().getCurrentLocation() + " " +
                    allocation.getExecutionMode().getMovetoLocation());
        }
    }

    public static String genPathFromConstants(String filename, HashMap<String, Object> ConstantsJson) {
        return castString(castDict(castDict(ConstantsJson.get("Paths")).get("Folders")).get(castString(castDict(castDict(ConstantsJson.get("Paths")).get("Files")).get(filename)))) + filename;
    }
}

