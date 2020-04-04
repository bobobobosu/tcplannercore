package bo.tc.tcplanner.SwiftGui;

import bo.tc.tcplanner.app.RMIInterface;
import bo.tc.tcplanner.app.Toolbox;
import bo.tc.tcplanner.datastructure.TimelineEntry;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import bo.tc.tcplanner.domain.solver.moves.SetValueMove;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StartStopGui extends JPanel {
    public ScoreDirector<Schedule> guiScoreDirector;
    public Schedule guiSchedule;
    private RMIInterface rmiInterface;
    JTable table;
    JTextArea detailTextField;
    List<Allocation> guiAllocationList;
    SwingWorker<Void, Void> consoleWorker;
    // current
    Allocation selectedAllocation = null;
    boolean isSelecting = false;
    private JComboBox<TimelineEntry> timelineEntryComboBox;
    private JComboBox<Integer> progressDeltaComboBox;
    private JComboBox<Integer> delayComboBox;
    public int ggg = 0;

    public StartStopGui(RMIInterface rmiInterface) {
        initializeScoreDirector();
        initializeUI();
        this.rmiInterface = rmiInterface;
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
                    allocation.getTimelineEntry().getTimelineProperty().getTimelineid(),
                    allocation.getProgressdelta(),
                    allocation.getDelay(),
                    dateString(allocation),
                    allocation.getPlannedDuration(),
                    allocation.getTimelineEntry(),
                    scoreString(allocation),
                    allocation.getTimelineEntry().getResourceStateChange().getResourceChange().toString()
            });
        }
        tableModel.fireTableDataChanged();
        isSelecting = true;
    }

    private void initializeScoreDirector() {
        SolverFactory<Schedule> solverFactory = SolverFactory.createFromXmlResource("solverPhase1.xml");
        guiScoreDirector = solverFactory.getScoreDirectorFactory().buildScoreDirector();
    }

    public void refreshSchedule() throws RemoteException {
        guiSchedule = rmiInterface.getCurrentSchedule();
        guiAllocationList = guiSchedule.getCondensedAllocationList();
        populateSchedule();
        this.revalidate();
        this.repaint();
    }

    private void initializeUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(900, 600));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        add(buttonPanel, BorderLayout.PAGE_START);

        JButton b = new JButton("STOP");
        b.addActionListener(e -> {
            try {
                rmiInterface.stopSolver();
                refreshSchedule();
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        });
        buttonPanel.add(b);

        JButton b2 = new JButton("START");
        b2.addActionListener(e -> {
            try {
                rmiInterface.startSolver(guiSchedule);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        });
        buttonPanel.add(b2);

        JButton b3 = new JButton("RESET");
        b3.addActionListener(e -> {
            try {
                rmiInterface.resetSolver();
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        });
        buttonPanel.add(b3);

        JButton b4 = new JButton("REFRESH");
        b4.addActionListener(e -> {
            try {
                refreshSchedule();
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        });
        buttonPanel.add(b4);


        JCheckBox c1 = new JCheckBox("CONSOLE");
        c1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (c1.isSelected()) {
                    consoleWorker = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            while (!this.isCancelled()) detailTextField.setText(rmiInterface.getConsoleBuffer());
                            return null;
                        }
                    };
                    consoleWorker.execute();
                } else {
                    if (consoleWorker != null) {
                        consoleWorker.cancel(true);
                        consoleWorker = null;
                    }
                }
            }
        });
        buttonPanel.add(c1);

        // Table
        table = new JTable();
        DefaultTableModel timelineTableModel = new DefaultTableModel();
        timelineTableModel.setColumnIdentifiers(
                new Object[]{"Row", "Id", "%", "Delay", "Date", "Duration", "Title", "Score", "Resource Change"});
        table.setModel(timelineTableModel);
        table.setPreferredScrollableViewportSize(new Dimension(table.getPreferredSize().width, 700));
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.getColumn("Row").setPreferredWidth(40);
        table.getColumn("Id").setPreferredWidth(40);
        table.getColumn("%").setPreferredWidth(40);
        table.getColumn("Delay").setPreferredWidth(40);
        table.getColumn("Date").setPreferredWidth(170);
        table.getColumn("Duration").setPreferredWidth(80);
        table.getColumn("Title").setPreferredWidth(200);
        table.getColumn("Score").setPreferredWidth(100);
        table.getColumn("Resource Change").setPreferredWidth(200);

        table.getSelectionModel().addListSelectionListener(event -> {
            if (table.getSelectedRow() != -1 && guiAllocationList != null) {
                isSelecting = false;
                selectedAllocation = guiAllocationList.get(table.getSelectedRow());
                detailTextField.setText(detailString(selectedAllocation));
                detailTextField.setCaretPosition(0);
                progressDeltaComboBox.removeAllItems();
                selectedAllocation.getProgressDeltaRange().createOriginalIterator().forEachRemaining(x ->
                        progressDeltaComboBox.addItem(x));
                progressDeltaComboBox.setSelectedItem(selectedAllocation.getProgressdelta());
                delayComboBox.removeAllItems();
                selectedAllocation.getDelayRange().createOriginalIterator().forEachRemaining(x ->
                        delayComboBox.addItem(x));
                delayComboBox.setSelectedItem(selectedAllocation.getDelay());
                timelineEntryComboBox.removeAllItems();
                guiSchedule.getTimelineEntryList().forEach(x ->
                        timelineEntryComboBox.addItem(x));
                timelineEntryComboBox.setSelectedItem(selectedAllocation.getTimelineEntry());
                isSelecting = true;
            }
        });

        JScrollPane pane = new JScrollPane(table);
        pane.setPreferredSize(getPreferredSize());
        add(pane);


        // ComboBoxes
        progressDeltaComboBox = new JComboBox();
        buttonPanel.add(progressDeltaComboBox);
        timelineEntryComboBox = new JComboBox();
        buttonPanel.add(timelineEntryComboBox);
        delayComboBox = new JComboBox();
        buttonPanel.add(delayComboBox);
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


        // Add Textarea in to middle panel
        detailTextField = new JTextArea();
        detailTextField.setPreferredSize(new Dimension(getPreferredSize().width, 20000));
        detailTextField.setFont(new Font("MingLiU", Font.PLAIN, 12));
        detailTextField.setEditable(false); // set textArea non-editable
        JScrollPane pane2 = new JScrollPane(detailTextField);
        pane2.setPreferredSize(new Dimension(getPreferredSize().width, 400));
        pane2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(pane2);
    }

    public void showFrame() {
        this.setOpaque(true);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("TCPlanner");
        frame.setContentPane(this);
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
        HardMediumSoftLongScore thisScore = null;
        if (guiScoreDirector.getIndictmentMap().containsKey(allocation)) {
            thisScore = (HardMediumSoftLongScore) guiScoreDirector.getIndictmentMap().get(allocation).getScore();
        }
        return thisScore != null && thisScore.getHardScore() != 0 ?
                thisScore.toShortString() : "";
    }

    private String detailString(Allocation allocation) {
        if (allocation.getStartDate() == null) return "";
        Toolbox.PrettyPrintAlloc printAlloc = new Toolbox.PrettyPrintAlloc(guiScoreDirector);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return printAlloc.prettyAllocation(allocation) + "\n" + gson.toJson(allocation.getTimelineEntry());
    }
}