package io.dm.panel.ui;

import io.dm.panel.core.ActionDispatcher;
import io.dm.panel.core.LogFeed;
import io.dm.panel.core.PanelState;
import io.dm.panel.core.ServerSnapshot;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

/**
 * The live console: a merged, colorized, filterable view of everything the server is
 * doing — tee'd {@code System.out}/{@code System.err} (which, with the slf4j-simple
 * binding, also carries all SLF4J log output), plus panel action audit lines.
 *
 * <p>Also hosts the generic "run command as ‹online staff player›" box, which pipes a
 * string straight into the real {@code CommandHandler} via {@link ActionDispatcher},
 * giving full coverage of every existing {@code ::} debug command.
 *
 * <p>Incoming lines are buffered and drained on an {@link AnimationTimer} (once per FX
 * frame) rather than one {@code Platform.runLater} per line, so heavy logging can't
 * flood the FX thread.
 */
public final class ConsoleTab implements PanelTab {

    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    private final ObservableList<LogFeed.LogLine> backing = FXCollections.observableArrayList();
    private final FilteredList<LogFeed.LogLine> filtered = new FilteredList<>(backing, l -> true);
    private final ListView<LogFeed.LogLine> list = new ListView<>(filtered);

    private final ConcurrentLinkedQueue<LogFeed.LogLine> incoming = new ConcurrentLinkedQueue<>();
    private final java.util.function.Consumer<LogFeed.LogLine> subscriber = incoming::add;

    private final ComboBox<String> filterType = new ComboBox<>();
    private final TextField searchField = new TextField();
    private final CheckBox autoScroll = new CheckBox("Auto-scroll");

    private final ComboBox<String> actingStaff = new ComboBox<>();
    private final TextField commandField = new TextField();

    @Override
    public String title() {
        return "Console";
    }

    @Override
    public Node build() {
        Label header = new Label("Live Console");
        header.getStyleClass().add("section-header");

        // Filters
        filterType.getItems().addAll("ALL", "INFO", "WARN", "ERROR", "STDOUT", "STDERR", "PANEL");
        filterType.setValue("ALL");
        filterType.valueProperty().addListener((o, a, b) -> applyFilter());
        searchField.setPromptText("Search…");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((o, a, b) -> applyFilter());
        HBox.setHgrow(searchField, Priority.ALWAYS);
        autoScroll.setSelected(true);
        autoScroll.getStyleClass().add("auto-scroll-check");
        Button clear = new Button("Clear");
        clear.getStyleClass().add("action-button");
        clear.setOnAction(e -> backing.clear());
        HBox filterBar = new HBox(10, new Label("Filter:"), filterType, searchField, autoScroll, clear);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        // Console list
        list.getStyleClass().add("console-list");
        list.setCellFactory(v -> new LogCell());
        VBox.setVgrow(list, Priority.ALWAYS);

        // Command runner
        actingStaff.setPromptText("Act as staff…");
        actingStaff.setPrefWidth(160);
        actingStaff.valueProperty().addListener((o, a, b) -> PanelState.setActingStaffName(b));
        commandField.setPromptText("Type a command, e.g. bc Hello world  (or ::master)");
        commandField.getStyleClass().add("command-field");
        commandField.setOnAction(e -> runCommand());
        HBox.setHgrow(commandField, Priority.ALWAYS);
        Button run = new Button("Run");
        run.getStyleClass().addAll("action-button", "primary-button");
        run.setOnAction(e -> runCommand());
        HBox commandBar = new HBox(10, new Label("Run as:"), actingStaff, commandField, run);
        commandBar.setAlignment(Pos.CENTER_LEFT);
        commandBar.getStyleClass().add("command-bar");

        // Seed with existing buffer, then go live.
        backing.addAll(LogFeed.INSTANCE.snapshot());
        LogFeed.INSTANCE.subscribe(subscriber);
        startDrainLoop();

        VBox root = new VBox(10, header, filterBar, list, commandBar);
        root.setPadding(new Insets(18));
        root.getStyleClass().add("tab-root");
        return root;
    }

    private void runCommand() {
        String cmd = commandField.getText();
        if (cmd == null || cmd.isBlank()) {
            return;
        }
        ActionDispatcher.runCommand(actingStaff.getValue(), cmd);
        commandField.clear();
    }

    private void startDrainLoop() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (incoming.isEmpty()) {
                    return;
                }
                boolean atBottom = autoScroll.isSelected();
                List<LogFeed.LogLine> drained = new ArrayList<>();
                LogFeed.LogLine line;
                int budget = 500; // cap per frame to stay responsive under log storms
                while (budget-- > 0 && (line = incoming.poll()) != null) {
                    drained.add(line);
                }
                backing.addAll(drained);
                // Trim to the same bound as the feed itself.
                int overflow = backing.size() - LogFeed.MAX_LINES;
                if (overflow > 0) {
                    backing.remove(0, overflow);
                }
                if (atBottom && !filtered.isEmpty()) {
                    list.scrollTo(filtered.size() - 1);
                }
            }
        }.start();
    }

    private void applyFilter() {
        String type = filterType.getValue();
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        Predicate<LogFeed.LogLine> typePred = switch (type == null ? "ALL" : type) {
            case "STDOUT", "STDERR", "PANEL" -> l -> l.source().equalsIgnoreCase(type)
                    || l.level().equalsIgnoreCase(type);
            case "INFO", "WARN", "ERROR" -> l -> l.level().equalsIgnoreCase(type)
                    || l.message().toUpperCase().contains(" " + type + " ")
                    || l.message().toUpperCase().contains(type);
            default -> l -> true;
        };
        Predicate<LogFeed.LogLine> searchPred = q.isEmpty()
                ? l -> true
                : l -> l.message().toLowerCase().contains(q);
        filtered.setPredicate(typePred.and(searchPred));
    }

    @Override
    public void onSnapshot(ServerSnapshot s) {
        // Refresh the "act as staff" choices from online players, preserving selection.
        String selected = actingStaff.getValue();
        ObservableList<String> staff = FXCollections.observableArrayList();
        for (ServerSnapshot.PlayerRow r : s.onlinePlayers()) {
            if (r.staff()) {
                staff.add(r.name());
            }
        }
        actingStaff.setItems(staff);
        if (selected != null && staff.contains(selected)) {
            actingStaff.setValue(selected);
        }
    }

    /** Renders one log line: {@code [HH:mm:ss] [source] message}, colored by level/source. */
    private static final class LogCell extends ListCell<LogFeed.LogLine> {
        @Override
        protected void updateItem(LogFeed.LogLine item, boolean empty) {
            super.updateItem(item, empty);
            getStyleClass().removeAll("log-info", "log-warn", "log-error", "log-stdout", "log-stderr", "log-panel");
            if (empty || item == null) {
                setText(null);
                return;
            }
            setText("[" + TIME.format(Instant.ofEpochMilli(item.timestampMs())) + "] "
                    + "[" + item.source() + "] " + item.message());
            String msgUpper = item.message().toUpperCase();
            if (item.level().equalsIgnoreCase("PANEL")) {
                getStyleClass().add("log-panel");
            } else if (item.level().equalsIgnoreCase("STDERR") || msgUpper.contains("ERROR") || msgUpper.contains("EXCEPTION")) {
                getStyleClass().add("log-error");
            } else if (msgUpper.contains("WARN")) {
                getStyleClass().add("log-warn");
            } else if (item.source().equals("stdout")) {
                getStyleClass().add("log-stdout");
            } else {
                getStyleClass().add("log-info");
            }
        }
    }
}
