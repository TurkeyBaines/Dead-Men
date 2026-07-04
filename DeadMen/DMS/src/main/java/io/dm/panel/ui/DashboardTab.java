package io.dm.panel.ui;

import io.dm.panel.core.PanelFormat;
import io.dm.panel.core.ServerSnapshot;
import io.dm.panel.ui.components.StatTile;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Live overview: server health (uptime, tick rate/lag, queued tasks), population,
 * and tournament state with countdown. A responsive grid of {@link StatTile}s.
 */
public final class DashboardTab implements PanelTab {

    private final StatTile uptime = new StatTile("Uptime", "accent-value");
    private final StatTile players = new StatTile("Players Online", "accent-value");
    private final StatTile world = new StatTile("World");
    private final StatTile tickRate = new StatTile("Tick / Period");
    private final StatTile lag = new StatTile("Last / Peak Tick");
    private final StatTile slow = new StatTile("Slow Ticks / Queue");
    private final StatTile tournamentStage = new StatTile("Tournament Stage", "accent-value");
    private final StatTile countdown = new StatTile("Time Until Change", "accent-value");
    private final StatTile overworld = new StatTile("Overworld Pop.");
    private final StatTile citadel = new StatTile("Citadel Pop.");
    private final StatTile currentEvent = new StatTile("Live Event");
    private final StatTile season = new StatTile("Tournament Code");

    private final Label configLabel = new Label("Config: --");
    private final Label nextConfigLabel = new Label("Next: --");

    @Override
    public String title() {
        return "Dashboard";
    }

    @Override
    public Node build() {
        Label header = new Label("Server Overview");
        header.getStyleClass().add("section-header");

        GridPane grid = new GridPane();
        grid.getStyleClass().add("tile-grid");
        grid.setHgap(14);
        grid.setVgap(14);

        StatTile[] tiles = {
                uptime, players, world,
                tickRate, lag, slow,
                tournamentStage, countdown, currentEvent,
                overworld, citadel, season
        };
        int cols = 3;
        for (int i = 0; i < tiles.length; i++) {
            GridPane.setColumnIndex(tiles[i], i % cols);
            GridPane.setRowIndex(tiles[i], i / cols);
            grid.getChildren().add(tiles[i]);
        }

        configLabel.getStyleClass().add("config-line");
        nextConfigLabel.getStyleClass().add("config-line");
        VBox configBox = new VBox(4, configLabel, nextConfigLabel);
        configBox.getStyleClass().add("config-box");

        VBox root = new VBox(16, header, grid, configBox);
        root.setPadding(new Insets(18));
        root.getStyleClass().add("tab-root");
        return root;
    }

    @Override
    public void onSnapshot(ServerSnapshot s) {
        uptime.setValue(PanelFormat.durationMs(s.uptimeMs()));
        players.setValue(s.playersOnline());
        world.setValue(s.worldName() + " (" + s.worldId() + ")\n" + s.worldStage() + " · " + s.worldType());
        tickRate.setValue("#" + PanelFormat.number(s.tickCount()) + "\n" + s.tickPeriodMs() + "ms");
        long period = s.tickPeriodMs();
        lag.setValue(s.lastExecutionMs() + "ms / " + s.slowestExecutionMs() + "ms");
        lag.setStatus(s.lastExecutionMs() < period ? "stat-ok"
                : s.lastExecutionMs() < period * 2 ? "stat-warn" : "stat-err");
        slow.setValue(s.slowExecutions() + " / " + s.queuedTasks());
        slow.setStatus(s.queuedTasks() <= 4 ? "stat-ok"
                : s.queuedTasks() <= 20 ? "stat-warn" : "stat-err");
        tournamentStage.setValue(s.tournamentStage());
        countdown.setValue(s.timeUntilChange());
        overworld.setValue(s.overworldPopulation());
        citadel.setValue(s.citadelPopulation());
        currentEvent.setValue(s.currentEventName());
        season.setValue(s.seasonCode());
        configLabel.setText("Config: " + s.configName());
        nextConfigLabel.setText("Next: " + s.nextConfigName());
    }
}
