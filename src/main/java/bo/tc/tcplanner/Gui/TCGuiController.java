package bo.tc.tcplanner.Gui;

import bo.tc.tcplanner.app.RMIInterface;
import bo.tc.tcplanner.app.TCApp;
import bo.tc.tcplanner.app.Toolbox;
import bo.tc.tcplanner.datastructure.ResourceElement;
import bo.tc.tcplanner.datastructure.TimelineEntry;
import bo.tc.tcplanner.domain.Allocation;
import bo.tc.tcplanner.domain.Schedule;
import bo.tc.tcplanner.domain.solver.moves.SetValueMove;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.rmi.RemoteException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class TCGuiController {
    // Objects
    private TCApp application;
    private RMIInterface rmiInterface;
    private ScoreDirector<Schedule> guiScoreDirector;

    // Data
    public Schedule guiSchedule;
    ObservableList<Allocation> guiAllocationList;
    Queue<String> consoleBuffer;

    // Components
    public Button start_btn;
    public Button stop_btn;
    public Button reset_btn;
    public Button refresh_btn;
    public ToggleButton update_toggle;
    public TextArea console_textarea;
    public TextArea editor_textarea;
    public TableView<Allocation> editor_table;
    public ProgressIndicator solving_progress;

    public TCGuiController() {

    }

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            rmiInterface = application.getRmiInterface();
            initializeScoreDirector();

            // Buttons
            Service<Void> newSolutionService = new Service<>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<>() {
                        @Override
                        protected Void call() {
                            while (!isCancelled()) {
                                double progress = 0;
                                try {
                                    progress = rmiInterface.getSolvingProgress();
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                                double finalProgress = progress;
                                Platform.runLater(() -> {
                                    solving_progress.setProgress(finalProgress);
                                });
                            }
                            return null;
                        }
                    };
                }
            };
            newSolutionService.restart();

            start_btn.setOnAction(e -> {
                try {
                    rmiInterface.startSolver(guiSchedule);
                    newSolutionService.restart();
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            });
            stop_btn.setOnAction(e -> {
                try {
                    rmiInterface.stopSolver();
                    refreshSchedule();
                    newSolutionService.cancel();
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            });
            reset_btn.setOnAction(e -> {
                try {
                    rmiInterface.resetSolver();
                } catch (RemoteException ex) {
                    ex.printStackTrace();

                }

            });
            refresh_btn.setOnAction(e -> {
                try {
                    refreshSchedule();
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            });

            // Toggles
            consoleBuffer = new CircularFifoQueue<>(5);
            Service<Void> consoleService = new Service<>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<>() {
                        @Override
                        protected Void call() {
                            while (!isCancelled()) {
                                try {
                                    consoleBuffer.add(rmiInterface.getConsoleBuffer());
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                                Platform.runLater(() -> {
                                    console_textarea.setText(String.join("", consoleBuffer));
                                    console_textarea.setScrollTop(Double.MAX_VALUE);
                                });
                            }
                            return null;
                        }
                    };
                }
            };

            update_toggle.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (newValue) {
                    consoleService.restart();
                } else {
                    consoleService.cancel();
                }
            });

            // Table
            TableColumn<Allocation, Integer> rowColumn = new TableColumn<>("Row");
            rowColumn.setCellValueFactory(x ->
                    new SimpleObjectProperty<>(x.getValue().getTimelineEntry().getTimelineProperty().getRownum()));
            rowColumn.setPrefWidth(40);

            TableColumn<Allocation, Integer> idColumn = new TableColumn<>("Id");
            idColumn.setCellValueFactory(x ->
                    new SimpleObjectProperty<>(x.getValue().getTimelineEntry().getTimelineProperty().getTimelineid()));
            idColumn.setPrefWidth(40);

            TableColumn<Allocation, Integer> percentColumn = new TableColumn<>("%");
            percentColumn.setCellValueFactory(new PropertyValueFactory<>("progressdelta"));
            percentColumn.setCellFactory(x -> new TableCell<>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    Allocation thisObject = this.getTableRow().getItem();
                    if (empty || thisObject == null) {
                        setText(null);
                    } else {
                        ComboBox<Integer> cb = new ComboBox<>(FXCollections.observableArrayList());
                        thisObject.getProgressDeltaRange().createOriginalIterator()
                                .forEachRemaining(x -> cb.getItems().add(x));
                        cb.getSelectionModel().select(thisObject.getProgressdelta());
                        cb.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
                            SetValueMove setValueMove = new SetValueMove(thisObject,
                                    newValue, null, null);
                            if (setValueMove.isMoveDoable(guiScoreDirector)) {
                                setValueMove.doMove(guiScoreDirector);
                                populateSchedule();
                            } else {
                                cb.getSelectionModel().select(oldValue);
                            }
                        });
                        scrollComboBox(cb);
                        setGraphic(cb);
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    }
                }
            });
            percentColumn.setPrefWidth(80);

            TableColumn<Allocation, Integer> delayColumn = new TableColumn<>("delay");
            delayColumn.setCellValueFactory(new PropertyValueFactory<>("delay"));
            delayColumn.setCellFactory(x -> new TableCell<>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    Allocation thisObject = this.getTableRow().getItem();
                    if (empty || thisObject == null) {
                        setText(null);
                    } else {
                        ComboBox<Integer> cb = new ComboBox<>(FXCollections.observableArrayList());
                        thisObject.getDelayRange().createOriginalIterator()
                                .forEachRemaining(x -> cb.getItems().add(x));
                        cb.getSelectionModel().select(thisObject.getDelay());
                        cb.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
                            SetValueMove setValueMove = new SetValueMove(thisObject,
                                    null, null, newValue);
                            if (setValueMove.isMoveDoable(guiScoreDirector)) {
                                setValueMove.doMove(guiScoreDirector);
                                populateSchedule();
                            } else {
                                cb.getSelectionModel().select(oldValue);
                            }
                        });
                        scrollComboBox(cb);
                        setGraphic(cb);
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    }
                }
            });
            delayColumn.setPrefWidth(100);

            TableColumn<Allocation, String> dateColumn = new TableColumn<>("Date");
            dateColumn.setCellValueFactory(x ->
                    new SimpleObjectProperty<>(dateString(x.getValue())));
            dateColumn.setPrefWidth(170);

            TableColumn<Allocation, Duration> durationColumn = new TableColumn<>("Duration");
            durationColumn.setCellValueFactory(x ->
                    new SimpleObjectProperty<>(x.getValue().getPlannedDuration()));
            durationColumn.setPrefWidth(80);

            TableColumn<Allocation, TimelineEntry> titleColumn = new TableColumn<>("Entry");
            titleColumn.setCellValueFactory(x ->
                    new SimpleObjectProperty<>(x.getValue().getTimelineEntry()));
            titleColumn.setCellFactory(x -> new TableCell<>() {
                @Override
                protected void updateItem(TimelineEntry item, boolean empty) {
                    super.updateItem(item, empty);
                    Allocation thisObject = this.getTableRow().getItem();
                    if (empty || thisObject == null) {
                        setText(null);
                    } else {
                        ComboBox<TimelineEntry> cb = new ComboBox<>(
                                FXCollections.observableArrayList(thisObject.getTimelineEntryRange()));
                        cb.getSelectionModel().select(thisObject.getTimelineEntry());
                        cb.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
                            SetValueMove setValueMove = new SetValueMove(thisObject,
                                    null, newValue, null);
                            if (setValueMove.isMoveDoable(guiScoreDirector)) {
                                setValueMove.doMove(guiScoreDirector);
                                populateSchedule();
                            } else {
                                cb.getSelectionModel().select(oldValue);
                            }
                        });
                        scrollComboBox(cb);
                        setGraphic(cb);
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    }
                }
            });
            titleColumn.setPrefWidth(200);

            TableColumn<Allocation, String> scoreColumn = new TableColumn<>("Score");
            scoreColumn.setCellValueFactory(x ->
                    new SimpleObjectProperty<>(scoreString(x.getValue())));
            scoreColumn.setPrefWidth(100);

            TableColumn<Allocation, Map<String, List<ResourceElement>>> resourceColumn = new TableColumn<>("Resource Change");
            resourceColumn.setCellValueFactory(x ->
                    new SimpleObjectProperty<>(x.getValue().getTimelineEntry().getResourceStateChange().getResourceChange()));
            resourceColumn.setPrefWidth(200);

            editor_table.getColumns().addAll(Arrays.asList(
                    rowColumn, idColumn, percentColumn, delayColumn, dateColumn, durationColumn, titleColumn, scoreColumn, resourceColumn
            ));
            editor_table.getSelectionModel().selectedItemProperty().addListener(
                    (observableValue, oldValue, newValue) -> editor_textarea.setText(detailString(newValue)));
        });
    }

    private void initializeScoreDirector() {
        SolverFactory<Schedule> solverFactory = SolverFactory.createFromXmlResource("solverPhase1.xml");
        guiScoreDirector = solverFactory.getScoreDirectorFactory().buildScoreDirector();
    }

    public void refreshSchedule() throws RemoteException {
        guiSchedule = rmiInterface.getCurrentSchedule();
        guiAllocationList = FXCollections.observableArrayList();
        guiAllocationList.addAll(guiSchedule.getCondensedAllocationList());
        populateSchedule();

    }

    private void populateSchedule() {
        if (guiSchedule == null) return;
        guiScoreDirector.setWorkingSolution(guiSchedule);
        guiScoreDirector.triggerVariableListeners();
        guiScoreDirector.calculateScore();

        // populate value
        editor_table.setItems(FXCollections.observableList(guiAllocationList));
        editor_table.refresh();
    }


    // Tools
    private String dateString(Allocation allocation) {
        if (allocation == null || allocation.getStartDate() == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm");
        String datetime = formatter.format(
                allocation.getStartDate().withZoneSameInstant(ZoneId.systemDefault())) + " ~ " +
                formatter.format(
                        allocation.getEndDate().withZoneSameInstant(ZoneId.systemDefault()));
        return datetime;
    }

    private String scoreString(Allocation allocation) {
        if (allocation == null) return "";
        HardMediumSoftLongScore thisScore = null;
        if (guiScoreDirector.getIndictmentMap().containsKey(allocation)) {
            thisScore = (HardMediumSoftLongScore) guiScoreDirector.getIndictmentMap().get(allocation).getScore();
        }
        return thisScore != null && thisScore.getHardScore() != 0 ?
                thisScore.toShortString() : "";
    }

    private String detailString(Allocation allocation) {
        if (allocation == null || allocation.getStartDate() == null) return "";
        Toolbox.PrettyPrintAlloc printAlloc = new Toolbox.PrettyPrintAlloc(guiScoreDirector);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return printAlloc.prettyAllocation(allocation) + "\n" + gson.toJson(allocation.getTimelineEntry());
    }

    public static String getLastLines(String string, int numLines) {
        List<String> lines = Arrays.asList(string.split(System.lineSeparator()));
        return String.join("", lines.subList(Math.max(0, lines.size() - numLines), lines.size()));
    }

    private void scrollComboBox(ComboBox cb) {
        try {
            ListView list = (ListView) ((ComboBoxListViewSkin) cb.getSkin()).getPopupContent();
            list.scrollTo(Math.max(0, cb.getSelectionModel().getSelectedIndex()));
        }catch (Exception ignore){
        }
    }

    public TCApp getApplication() {
        return application;
    }

    public void setApplication(TCApp application) {
        this.application = application;
    }

}
