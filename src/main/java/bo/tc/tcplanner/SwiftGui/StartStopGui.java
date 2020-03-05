package bo.tc.tcplanner.SwiftGui;

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.app.SolverThread;
import bo.tc.tcplanner.datastructure.ResourceElement;
import bo.tc.tcplanner.datastructure.TimelineEntry;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import bo.tc.tcplanner.domain.solver.moves.SetValueMove;
import com.google.common.collect.Lists;
import com.jakewharton.fliptables.FlipTable;
import org.optaplanner.core.api.domain.valuerange.ValueRangeFactory;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StartStopGui extends JPanel {
    SolverThread solverThread;

    JTable table;
    JTextArea detailTextField;

    ScoreDirector<Schedule> guiScoreDirector;
    Schedule guiSchedule;
    List<Allocation> guiAllocationList;
    private JComboBox<TimelineEntry> timelineEntryComboBox;
    private JComboBox<Integer> progressDeltaComboBox;
    private JComboBox<Integer> delayComboBox;
    Allocation selectedAllocation = null;

    boolean isSelecting = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> StartStopGui.showFrame(null));
    }

    public StartStopGui(SolverThread solverThread) {
        initializeScoreDirector();
        initializeUI();
        this.solverThread = solverThread;
    }

    private void populateSchedule() {
        if (guiSchedule == null) return;
        isSelecting = false;
        guiScoreDirector.setWorkingSolution(guiSchedule);
        guiScoreDirector.triggerVariableListeners();
        guiScoreDirector.calculateScore();

        // populate value
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        tableModel.setRowCount(0);
        for (Allocation allocation : guiAllocationList) {
            tableModel.addRow(new Object[]{
                    allocation.getTimelineEntry().getTimelineProperty().getRownum(),
                    allocation.getProgressdelta(),
                    allocation.getDelay(),
                    dateString(allocation),
                    allocation.getPlannedDuration(),
                    allocation.getTimelineEntry(),
                    scoreString(allocation)
            });
        }
        tableModel.fireTableDataChanged();

        // populate editor
        timelineEntryComboBox.setModel(new DefaultComboBoxModel(guiSchedule.getTimelineEntryList().toArray()));
        delayComboBox.setModel(new DefaultComboBoxModel());
        progressDeltaComboBox.setModel(new DefaultComboBoxModel());
        ValueRangeFactory.createIntValueRange(0, 101, 1).createOriginalIterator()
                .forEachRemaining(x -> progressDeltaComboBox.addItem(x));
        ValueRangeFactory.createIntValueRange(0, 1200, 1).createOriginalIterator()
                .forEachRemaining(x -> delayComboBox.addItem(x));

        isSelecting = true;
    }

    private void initializeScoreDirector() {
        SolverFactory<Schedule> solverFactory = SolverFactory.createFromXmlResource("solverPhase1.xml");
        guiScoreDirector = solverFactory.getScoreDirectorFactory().buildScoreDirector();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(900, 1500));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        add(buttonPanel, BorderLayout.PAGE_START);

        JButton b = new JButton("STOP");
        b.addActionListener(e -> {
            solverThread.terminateSolver();
            guiSchedule = solverThread.currentSchedule;
            guiAllocationList = guiSchedule.getBriefAllocationList();
            populateSchedule();
        });
        buttonPanel.add(b);

        JButton b2 = new JButton("START");
        b2.addActionListener(e -> {
            solverThread.currentSchedule = guiSchedule;
            solverThread.resumeSolvers();
        });
        buttonPanel.add(b2);

        JButton b3 = new JButton("RESET");
        b3.addActionListener(e -> {
            solverThread.restartSolvers();
        });
        buttonPanel.add(b3);

        // Table
        table = new JTable();
        DefaultTableModel timelineTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                switch (getColumnName(column)) {
                    case "Row":
                        return false;
                    case "%":
                        return true;
                    case "Delay":
                        return true;
                    case "Date":
                        return false;
                    case "Duration":
                        return false;
                    case "Title":
                        return true;
                    case "Score":
                        return false;
                }
                return super.isCellEditable(row, column);
            }
        };
        timelineTableModel.setColumnIdentifiers(
                new Object[]{"Row", "%", "Delay", "Date", "Duration", "Title", "Score",});
        table.setModel(timelineTableModel);
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.getColumn("Row").setPreferredWidth(40);
        table.getColumn("%").setPreferredWidth(40);
        table.getColumn("Delay").setPreferredWidth(40);
        table.getColumn("Date").setPreferredWidth(170);
        table.getColumn("Duration").setPreferredWidth(80);
        table.getColumn("Title").setPreferredWidth(200);
        table.getColumn("Score").setPreferredWidth(200);

        table.getSelectionModel().addListSelectionListener(event -> {
            if (isSelecting && table.getSelectedRow() != -1 && guiAllocationList != null) {
                selectedAllocation = guiAllocationList.get(table.getSelectedRow());
                detailTextField.setText(detailString(selectedAllocation));
            }
        });

        JScrollPane pane = new JScrollPane(table);
        pane.setPreferredSize(getPreferredSize());
        add(pane, BorderLayout.LINE_START);

        // Add Textarea in to middle panel
        detailTextField = new JTextArea();
        detailTextField.setPreferredSize(new Dimension(getPreferredSize().width, 150));
        detailTextField.setFont(new Font("Consolas", Font.PLAIN, 12));
        detailTextField.setEditable(false); // set textArea non-editable
        JScrollPane pane2 = new JScrollPane(detailTextField);
        pane2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(pane2, BorderLayout.PAGE_END);

        // ComboBoxes
        progressDeltaComboBox = new JComboBox();
        timelineEntryComboBox = new JComboBox();
        delayComboBox = new JComboBox();
        timelineEntryComboBox.addItemListener(e -> {
            if (!isSelecting || selectedAllocation == null) return;
            SetValueMove setValueMove = new SetValueMove(
                    selectedAllocation,
                    null,
                    (TimelineEntry) e.getItem(),
                    null);

            setValueMove.doMove(guiScoreDirector);
            populateSchedule();
        });
        progressDeltaComboBox.addItemListener(e -> {
            if (!isSelecting || selectedAllocation == null) return;
            SetValueMove setValueMove = new SetValueMove(
                    selectedAllocation,
                    (Integer) e.getItem(),
                    null,
                    null);

            setValueMove.doMove(guiScoreDirector);
            populateSchedule();
        });
        delayComboBox.addItemListener(e -> {
            if (!isSelecting || selectedAllocation == null) return;
            SetValueMove setValueMove = new SetValueMove(
                    selectedAllocation,
                    null,
                    null,
                    (Integer) e.getItem());

            setValueMove.doMove(guiScoreDirector);
            populateSchedule();
        });
        table.getColumn("%").setCellEditor(new DefaultCellEditor(progressDeltaComboBox));
        table.getColumn("Title").setCellEditor(new DefaultCellEditor(timelineEntryComboBox));
        table.getColumn("Delay").setCellEditor(new DefaultCellEditor(delayComboBox));
    }

    public static void showFrame(SolverThread solverThread) {
        JPanel panel = new StartStopGui(solverThread);
        panel.setOpaque(true);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("TCPlanner");
        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);
    }

    private String dateString(Allocation allocation) {
        if (allocation.getStartDate() == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm");
        String datetime = formatter.format(
                allocation.getStartDate().withZoneSameInstant(ZoneId.systemDefault())) + " ~ " +
                formatter.format(
                        allocation.getEndDate().withZoneSameInstant(ZoneId.systemDefault()));
        return datetime;
    }

    private String scoreString(Allocation allocation) {
        BendableScore thisScore = null;
        if (guiScoreDirector.getIndictmentMap().containsKey(allocation)) {
            thisScore = (BendableScore) guiScoreDirector.getIndictmentMap().get(allocation).getScore();
        }
        return thisScore != null && Arrays.stream(thisScore.getHardScores()).anyMatch(x -> x != 0) ?
                Arrays.toString(thisScore.getHardScores()) : "";
    }

    private String detailString(Allocation allocation) {
        if (allocation.getStartDate() == null) return "";
        String[] timelineHeader = {"Row", "%", "Date", "Duration", "Location", "Restriction", "Task"};
        List<String[]> breiftimeline = new ArrayList<>();
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
        breiftimeline.add(timelineentry);
        return FlipTable.of(timelineHeader, breiftimeline.toArray(new String[breiftimeline.size()][]));
    }
}