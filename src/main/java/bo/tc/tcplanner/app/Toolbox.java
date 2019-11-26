package bo.tc.tcplanner.app;

import bo.tc.tcplanner.datastructure.TimelineBlock;
import bo.tc.tcplanner.datastructure.TimelineEntry;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.AllocationType;
import bo.tc.tcplanner.domain.Schedule;
import com.jakewharton.fliptables.FlipTable;
import org.apache.commons.lang3.StringUtils;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import static bo.tc.tcplanner.datastructure.converters.DataStructureBuilder.dummyExecutionMode;

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
        for (Solver<Schedule> solver : solverList) solver.terminateEarly();
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

    public static void printCurrentSolution(Schedule schedule, Solver<Schedule> solver, boolean showTimeline, String solvingStatus) {
        try {
            System.err.print("\033[H\033[2J");
            System.err.flush();
            String[] breakByRulesHeader = {"Break Up By Rule"};
            String[] timelineHeader = {"Row", "%", "Date", "Duration", "Location", "Restriction", "Score", "Task"};
            List<String[]> breakByRules = new ArrayList<>();
            Map<Allocation, Indictment> breakByTasks = new HashMap<>();
            List<String[]> timeline = new ArrayList<>();


            ScoreDirector<Schedule> scoreDirector = solver.getScoreDirectorFactory().buildScoreDirector();
            scoreDirector.setWorkingSolution(schedule);
            for (ConstraintMatchTotal constraintMatch : scoreDirector.getConstraintMatchTotals()) {
//                if (Arrays.stream(((BendableScore) constraintMatch.getScore()).getHardScores()).anyMatch(x -> x != 0))
                breakByRules.add(new String[]{constraintMatch.toString()});
            }
            for (Map.Entry<Object, Indictment> indictmentEntry : scoreDirector.getIndictmentMap().entrySet()) {
                if (indictmentEntry.getValue().getJustification() instanceof Allocation &&
                        Arrays.stream(((BendableScore) indictmentEntry.getValue().getScore()).getHardScores()).anyMatch(x -> x != 0)) {
                    Allocation matchAllocation = (Allocation) indictmentEntry.getValue().getJustification();
                    breakByTasks.put(matchAllocation, indictmentEntry.getValue());
                }
            }
            for (Allocation allocation : schedule.getAllocationList()) {
                if (allocation.getExecutionMode() != dummyExecutionMode) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm");
                    String datetime = formatter.format(OffsetMinutes2ZonedDatetime(allocation.getProject().getSchedule().getGlobalStartTime(),
                            allocation.getStartDate()).withZoneSameInstant(ZoneId.systemDefault())) + "\n" +
                            formatter.format(OffsetMinutes2ZonedDatetime(allocation.getProject().getSchedule().getGlobalStartTime(),
                                    allocation.getEndDate()).withZoneSameInstant(ZoneId.systemDefault()));
                    timeline.add(new String[]{
                            (allocation.getJob().getRownum() == null || allocation.getJob().getRownum() < 0) ? "****" : String.valueOf(allocation.getJob().getRownum()),
                            String.valueOf(allocation.getProgressdelta()),
                            datetime,
                            LocalTime.MIN.plus(Duration.ofMinutes(allocation.getEndDate() - allocation.getStartDate())).toString(),
                            "P:" + allocation.getPreviousStandstill() +
                                    "\nC:" + allocation.getExecutionMode().getCurrentLocation() +
                                    "\nM:" + allocation.getExecutionMode().getMovetoLocation()
                            ,
                            allocation.getJob().getChangeable() + "C/" +
                                    allocation.getJob().getMovable() + "M/" +
                                    allocation.getJob().getSplittable() + "S/" +
                                    ((allocation.getAllocationType() == AllocationType.Locked) ? 1 : 0) + "L",
                            (breakByTasks.containsKey(allocation) ?
                                    hardConstraintMatchToString(breakByTasks.get(allocation).getConstraintMatchSet()): ""),
                            allocation.getJob().getName() + " " + allocation.getIndex() + "\n" +
                                    allocation.getExecutionMode().getResourceStateChange().getResourceChange().toString().replaceAll("(.{60})", "$1\n")
                    });
                }
            }
            timeline.add(timelineHeader);
            solver.explainBestScore();
            if (showTimeline)
                System.err.println(FlipTable.of(timelineHeader, timeline.toArray(new String[timeline.size()][])));
            System.err.println(FlipTable.of(breakByRulesHeader, breakByRules.toArray(new String[breakByRules.size()][])));
            System.err.println("Status: " + solvingStatus
                    + " " + new SimpleDateFormat("dd-MM HH:mm").format(new Date())
                    + " " + scoreDirector.calculateScore().toShortString());
            SolverThread.logger.debug("\n" + FlipTable.of(timelineHeader, timeline.toArray(new String[timeline.size()][])));
            SolverThread.logger.info("\n" + FlipTable.of(breakByRulesHeader, breakByRules.toArray(new String[breakByRules.size()][])));
            SolverThread.logger.info("\n" + "Status: " + solvingStatus
                    + " " + new SimpleDateFormat("dd-MM HH:mm").format(new Date())
                    + " " + scoreDirector.calculateScore().toShortString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String genPathFromConstants(String filename, HashMap<String, Object> ConstantsJson) {
        return castString(castDict(castDict(ConstantsJson.get("Paths")).get("Folders")).get(castString(castDict(castDict(ConstantsJson.get("Paths")).get("Files")).get(filename)))) + filename;
    }

    public static String hardConstraintMatchToString(Set<ConstraintMatch> ConstraintMatchSet){
        StringBuilder result = new StringBuilder();
        Iterator<ConstraintMatch> constraintMatchSetIterator = ConstraintMatchSet.iterator();
        while (constraintMatchSetIterator.hasNext()){
            ConstraintMatch constraintMatch = constraintMatchSetIterator.next();
            if(Arrays.stream(((BendableScore) constraintMatch.getScore()).getHardScores()).anyMatch(x -> x != 0)){
                result.append(constraintMatch.getConstraintName())
                        .append("\n")
                        .append(Arrays.toString(((BendableScore) (constraintMatch.getScore())).getHardScores()))
                        .append("\n");
            }
        }
        return result.toString();
    }
}

