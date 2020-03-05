package bo.tc.tcplanner.app;

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.datastructure.ResourceElement;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jakewharton.fliptables.FlipTable;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static bo.tc.tcplanner.app.JsonServer.updateConsole;

public class Toolbox {
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

    public static Object jacksonDeepCopy(Object obj) {
        try {
            String serielizedStr = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            Map serielizedMap = new ObjectMapper().readValue(serielizedStr, Map.class);
            return new ObjectMapper().convertValue(serielizedMap, obj.getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

    public static void printCurrentSolution(Schedule schedule, boolean showTimeline, String solvingStatus) {
        try {
            //Debug
            List<Allocation> debugAllocationList = new ArrayList<>(schedule.focusedAllocationSet);

            System.err.print("\033[H\033[2J");
            System.err.flush();
            String[] breakByRulesHeader = {"Break Up By Rule"};
            String[] timelineHeader = {"Row", "%", "Date", "Duration", "Location", "Restriction", "Score", "Task"};
            List<String[]> breakByRules = new ArrayList<>();
            Map<Allocation, Indictment> breakByTasks = new HashMap<>();
            List<String[]> breiftimeline = new ArrayList<>();
            List<String[]> fulltimeline = new ArrayList<>();

            //New Entries
            List<String> newTimelineEntries = debugAllocationList.stream().filter(x -> x.getTimelineEntry().getTimelineProperty().getPlanningWindowType()
                    .equals(PropertyConstants.PlanningWindowTypes.types.Draft.name())).map(x -> x.getTimelineEntry().getTitle()).distinct().collect(Collectors.toList());

            ScoreDirector<Schedule> scoreDirector = SolverThread.getScoringScoreDirector();
            scoreDirector.setWorkingSolution(schedule);
            for (ConstraintMatchTotal constraintMatch : scoreDirector.getConstraintMatchTotals()) {
                if (Arrays.stream(((BendableScore) constraintMatch.getScore()).getHardScores()).anyMatch(x -> x != 0))
                    breakByRules.add(new String[]{constraintMatch.toString()});
            }
            for (Map.Entry<Object, Indictment> indictmentEntry : scoreDirector.getIndictmentMap().entrySet()) {
                if (indictmentEntry.getValue().getJustification() instanceof Allocation &&
                        Arrays.stream(((BendableScore) indictmentEntry.getValue().getScore()).getHardScores()).anyMatch(x -> x != 0)) {
                    Allocation matchAllocation = (Allocation) indictmentEntry.getValue().getJustification();
                    breakByTasks.put(matchAllocation, indictmentEntry.getValue());
                }
            }

            int maxList = 10;
            for (Allocation allocation : schedule.getAllocationList()) {
                if (allocation.isFocused()) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm");
                    String datetime = formatter.format(
                            allocation.getStartDate().withZoneSameInstant(ZoneId.systemDefault())) + "\n" +
                            formatter.format(
                                    allocation.getEndDate().withZoneSameInstant(ZoneId.systemDefault()));
                    String[] timelineentry = (new String[]{
                            ((allocation.getTimelineEntry().getTimelineProperty().getPlanningWindowType()
                                    .equals(PropertyConstants.PlanningWindowTypes.types.Draft.name())) ? "****" :
                                    allocation.getTimelineEntry().getTimelineProperty().getRownum())
                                    + "\n(" + allocation.getIndex() + ")\n" +
                                    allocation.getTimelineEntry().getTimelineProperty().getPlanningWindowType(),
                            allocation.getProgressdelta() + "\n" +
                                    (allocation.isPinned() ? "Pinned" : "") + "\n" +
                                    (allocation.isScored() ? "Scored" : ""),
                            datetime,
                            LocalTime.MIN.plus(Duration.between(
                                    allocation.getStartDate(), allocation.getEndDate())).toString(),
                            "P:" + allocation.getPreviousStandstill() +
                                    "\nC:" + allocation.getTimelineEntry().getHumanStateChange().getCurrentLocation() +
                                    "\nM:" + allocation.getTimelineEntry().getHumanStateChange().getMovetoLocation()
                            ,
                            allocation.getTimelineEntry().getChronoProperty().getChangeable() + "C/" +
                                    allocation.getTimelineEntry().getChronoProperty().getMovable() + "M/" +
                                    allocation.getTimelineEntry().getChronoProperty().getSplittable() + "S/" +
                                    (allocation.isHistory() ? 1 : 0) + "L",
                            (breakByTasks.containsKey(allocation) ?
                                    hardConstraintMatchToString(breakByTasks.get(allocation).getConstraintMatchSet()) : ""),
                            allocation.getTimelineEntry().getTitle() + " " + "\n" +
                                    allocation.getResourceElementMap().entrySet()
                                            .stream()
                                            .filter(entry -> entry.getValue()
                                                    .stream()
                                                    .mapToDouble(ResourceElement::getAmt)
                                                    .sum() != 0)
                                            .collect(Collectors.toMap(Map.Entry::getKey,
                                                    x -> x.getValue().stream()
                                                            .mapToDouble(ResourceElement::getAmt)
                                                            .sum()
                                            )).toString().replaceAll("(.{60})", "$1\n")
                    });
                    if ((breakByTasks.containsKey(allocation)) && maxList-- > 0) breiftimeline.add(timelineentry);
                    fulltimeline.add(timelineentry);
                }
            }
            breiftimeline.add(timelineHeader);
            fulltimeline.add(timelineHeader);

            String status = "\n"
                    + "Status: " + SolverThread.lastNewBestSolution[1] + "ms " + solvingStatus
                    + " \nScore:" + scoreDirector.calculateScore().toShortString()
                    + " \nRate: "
                    + ((SolverThread.lastNewBestSolution[3] != null ?
                    ((Score) SolverThread.lastNewBestSolution[3]).divide(
                            ((double) (long) SolverThread.lastNewBestSolution[1]) / 1000).toShortString()
                    : "")
                    + " \nAdded:" + newTimelineEntries.toString());
            if (showTimeline)
                System.err.println(updateConsole(FlipTable.of(timelineHeader, breiftimeline.toArray(new String[breiftimeline.size()][]))));
            System.err.println(updateConsole(FlipTable.of(breakByRulesHeader, breakByRules.toArray(new String[breakByRules.size()][]))));
            System.err.println(updateConsole(status));
            SolverThread.logger.debug("\n" + FlipTable.of(timelineHeader, fulltimeline.toArray(new String[fulltimeline.size()][])));
            SolverThread.logger.info("\n" + FlipTable.of(breakByRulesHeader, breakByRules.toArray(new String[breakByRules.size()][])));
            SolverThread.logger.info(status);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String genPathFromConstants(String filename, HashMap<String, Object> ConstantsJson) {
        return castString(castDict(castDict(ConstantsJson.get("Paths")).get("Folders")).get(castString(castDict(castDict(ConstantsJson.get("Paths")).get("Files")).get(filename)))) + filename;
    }


    public static String hardConstraintMatchToString(Set<ConstraintMatch> ConstraintMatchSet) {
        StringBuilder result = new StringBuilder();
        Iterator<ConstraintMatch> constraintMatchSetIterator = ConstraintMatchSet.iterator();
        while (constraintMatchSetIterator.hasNext()) {
            ConstraintMatch constraintMatch = constraintMatchSetIterator.next();
            if (Arrays.stream(((BendableScore) constraintMatch.getScore()).getHardScores()).anyMatch(x -> x < 0)) {
                result.append(constraintMatch.getConstraintName())
                        .append("\n")
                        .append(Arrays.toString(((BendableScore) (constraintMatch.getScore())).getHardScores()))
                        .append("\n");
            }
        }
        return result.toString();
    }
}

