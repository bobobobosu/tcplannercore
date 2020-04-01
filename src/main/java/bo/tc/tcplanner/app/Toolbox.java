package bo.tc.tcplanner.app;

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jakewharton.fliptables.FlipTable;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
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
    public static TrayIcon trayIcon;

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
        tray.remove(trayIcon);
        //If the icon is a file
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
        //Alternative (if the icon is on the classpath):
        //Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("icon.png"));

        trayIcon = new TrayIcon(image, "Tray Demo");
        //Let the system resize the image if needed
        trayIcon.setImageAutoSize(true);
        //Set tooltip text for the tray icon
        trayIcon.setToolTip("System tray icon demo");
        trayIcon.addActionListener(e -> {

        });
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

    public static void printCurrentSolution(Schedule schedule, boolean showTimeline) {
        try {
            //Debug
            List<Allocation> debugAllocationList = new ArrayList<>(schedule.focusedAllocationSet);

            System.err.print("\033[H\033[2J");
            System.err.flush();
            String[] breakByRulesHeader = {"Break Up By Rule"};
            String[] timelineHeader = {"Row", "%", "Date", "Duration", "Location", "Plan Restrict", "Move Restrict", "Score", "Task"};
            List<String[]> breiftimeline = new ArrayList<>();
            List<String[]> fulltimeline = new ArrayList<>();

            //New Entries
            List<String> newTimelineEntries = debugAllocationList.stream().filter(x -> x.getTimelineEntry().getTimelineProperty().getPlanningWindowType()
                    .equals(PropertyConstants.PlanningWindowTypes.types.Draft.name())).map(x -> x.getTimelineEntry().getTitle()).distinct().collect(Collectors.toList());

            ScoreDirector<Schedule> scoreDirector = SolverThread.getScoringScoreDirector();
            scoreDirector.setWorkingSolution(schedule);
            PrettyPrintAlloc printAlloc = new PrettyPrintAlloc(scoreDirector);
            List<String[]> breakByRules = printAlloc.breakByRules;
            Map<Allocation, Indictment> breakByTasks = printAlloc.breakByTasks;

            int maxList = 10;
            for (Allocation allocation : schedule.getAllocationList()) {
                if (allocation.isFocused()) {
                    String[] timelineentry = (new String[]{
                            printAlloc.timelineStr(allocation),
                            printAlloc.percentStr(allocation),
                            printAlloc.datetimeStr(allocation),
                            printAlloc.durationStr(allocation),
                            printAlloc.standstillStr(allocation),
                            printAlloc.plrestrictStr(allocation),
                            printAlloc.mvrestrictStr(allocation),
                            printAlloc.scoreStr(allocation),
                            printAlloc.titleStr(allocation)});
                    if ((breakByTasks.containsKey(allocation)) && maxList-- > 0) breiftimeline.add(timelineentry);
                    fulltimeline.add(timelineentry);
                }
            }
            breiftimeline.add(timelineHeader);
            fulltimeline.add(timelineHeader);

            String status = "\n"
                    + "Status: " + SolverThread.lastNewBestSolution[1] + "ms " +
                    (schedule.solverPhase != null ? schedule.solverPhase.name() : "")
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

    public static String hardConstraintMatchToString(Set<ConstraintMatch> ConstraintMatchSet) {
        StringBuilder result = new StringBuilder();
        Iterator<ConstraintMatch> constraintMatchSetIterator = ConstraintMatchSet.iterator();
        while (constraintMatchSetIterator.hasNext()) {
            ConstraintMatch constraintMatch = constraintMatchSetIterator.next();
            if (((HardMediumSoftLongScore) constraintMatch.getScore()).getHardScore() < 0) {
                result.append(constraintMatch.getConstraintName())
                        .append("[")
                        .append(((HardMediumSoftLongScore) constraintMatch.getScore()).getHardScore())
                        .append("]\n");
            }
        }
        return result.toString();
    }

    public static String genPathFromConstants(String filename, HashMap<String, Object> ConstantsJson) {
        return castString(castDict(castDict(ConstantsJson.get("Paths")).get("Folders")).get(castString(castDict(castDict(ConstantsJson.get("Paths")).get("Files")).get(filename)))) + filename;
    }

    public static class PrettyPrintAlloc {
        public List<String[]> breakByRules = new ArrayList<>();
        public Map<Allocation, Indictment> breakByTasks = new HashMap<>();
        ScoreDirector<Schedule> scoreDirector;

        public PrettyPrintAlloc(ScoreDirector<Schedule> scoreDirector) {
            this.scoreDirector = scoreDirector;
            this.scoreDirector.calculateScore();
            for (ConstraintMatchTotal constraintMatch : scoreDirector.getConstraintMatchTotals()) {
                if (((HardMediumSoftLongScore) (constraintMatch.getScore())).getHardScore() < 0)
                    breakByRules.add(new String[]{constraintMatch.toString()});
            }
            for (Map.Entry<Object, Indictment> indictmentEntry : scoreDirector.getIndictmentMap().entrySet()) {
                if (indictmentEntry.getValue().getJustification() instanceof Allocation &&
                        ((HardMediumSoftLongScore) indictmentEntry.getValue().getScore()).getHardScore() < 0) {
                    Allocation matchAllocation = (Allocation) indictmentEntry.getValue().getJustification();
                    breakByTasks.put(matchAllocation, indictmentEntry.getValue());
                }
            }
        }

        public String percentStr(Allocation allocation) {
            return allocation.getProgressdelta() + "\n/"
                    + (int) (+allocation.getTimelineEntry().getProgressChange().getProgressDelta() * 100);
        }

        public String timelineStr(Allocation allocation) {
            // rownum, {id}, [idx]
            int rownum = allocation.getTimelineEntry().getTimelineProperty().getRownum();
            Integer id = allocation.getTimelineEntry().getTimelineProperty().getTimelineid();
            int idx = allocation.getIndex();
            return ((allocation.getTimelineEntry().getTimelineProperty().getPlanningWindowType()
                    .equals(PropertyConstants.PlanningWindowTypes.types.Draft.name())) ? "****" : rownum)
                    + "\n{" + (id != null ? String.valueOf(id) : "N/A")
                    + "}\n[" + idx + "]";
        }

        public String durationStr(Allocation allocation) {
            return LocalTime.MIN.plus(Duration.between(
                    allocation.getStartDate(), allocation.getEndDate())).toString();
        }

        public String datetimeStr(Allocation allocation) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm");
            return formatter.format(
                    allocation.getStartDate().withZoneSameInstant(ZoneId.systemDefault())) + "\n" +
                    formatter.format(
                            allocation.getEndDate().withZoneSameInstant(ZoneId.systemDefault()));
        }

        public String standstillStr(Allocation allocation) {
            return "P:" + allocation.getPreviousStandstill() +
                    "\nC:" + allocation.getTimelineEntry().getHumanStateChange().getCurrentLocation() +
                    "\nM:" + allocation.getTimelineEntry().getHumanStateChange().getMovetoLocation();
        }

        public String mvrestrictStr(Allocation allocation) {
            return allocation.getTimelineEntry().getChronoProperty().getChangeable() + "C/" +
                    allocation.getTimelineEntry().getChronoProperty().getMovable() + "M/" +
                    allocation.getTimelineEntry().getChronoProperty().getSplittable() + "S\n" +
                    allocation.getTimelineEntry().getTimelineProperty().getPlanningWindowType() + "\n" +
                    (allocation.isPinned() ? "Pinned" : "") + "\n" +
                    (allocation.isScored() ? "Scored" : "");
        }

        public String plrestrictStr(Allocation allocation) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm");
            return ">" + formatter.format(
                    allocation.getTimelineEntry().getChronoProperty().getZonedAliveline()
                            .withZoneSameInstant(ZoneId.systemDefault())) + "\n" +
                    "<" + formatter.format(
                    allocation.getTimelineEntry().getChronoProperty().getZonedDeadline()
                            .withZoneSameInstant(ZoneId.systemDefault())) + "\n" +
                    "[" + allocation.getTimelineEntry().getHumanStateChange().getRequirementTimerange() + "]\n" +
                    "_" + allocation.getTimelineEntry().getHumanStateChange().getAdviceTimerange() + "_";
        }

        public String scoreStr(Allocation allocation) {
            return (breakByTasks.containsKey(allocation) ?
                    hardConstraintMatchToString(breakByTasks.get(allocation).getConstraintMatchSet()) : "");
        }

        public String titleStr(Allocation allocation) {

            return allocation.getTimelineEntry().getTitle() + " " + "\n" +
                    allocation.getResourceElementMap().entrySet().stream().filter(
                            x -> x.getValue().stream().anyMatch(y -> y.getAmt() < 0 ||
                                    y.getAmt() > allocation.getSchedule().getValueEntryMap().get(x.getKey()).getCapacity()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                            .toString().replace("], ", "]\n");
        }

        public String prettyAllocation(Allocation allocation) {
            String[] timelineHeader = {"Row", "%", "Date", "Duration", "Location", "Plan Restrict", "Move Restrict", "Score", "Task"};
            String[] timelineentry = (new String[]{
                    timelineStr(allocation),
                    percentStr(allocation),
                    datetimeStr(allocation),
                    durationStr(allocation),
                    standstillStr(allocation),
                    plrestrictStr(allocation),
                    mvrestrictStr(allocation),
                    scoreStr(allocation),
                    titleStr(allocation)});
            return FlipTable.of(timelineHeader, new String[][]{timelineentry});
        }
    }
}

