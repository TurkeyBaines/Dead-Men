package io.dm.panel.ui;

import io.dm.deadman.tournament.Tournament;
import io.dm.deadman.tournament.events.breach.Breach;
import io.dm.deadman.tournament.events.chest.StaticChest;
import io.dm.deadman.tournament.events.gold_cart.GoldCart;
import io.dm.deadman.tournament.events.koth.KingOfTheHill;
import io.dm.panel.core.ActionDispatcher;
import io.dm.panel.core.ServerSnapshot;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Deadman tournament control: force stage transitions, spawn/kill live events, and
 * view live team standings. All actions go through {@link ActionDispatcher}.
 */
public final class TournamentTab implements PanelTab {

    private final Label stageLabel = new Label("Stage: --");
    private final Label countdownLabel = new Label("Change in: --");
    private final Label eventLabel = new Label("Live event: --");
    private final Label configLabel = new Label("Config: --");
    private final Label nextConfigLabel = new Label("Next: --");

    private final ObservableList<ServerSnapshot.TeamRow> teams = FXCollections.observableArrayList();
    private final TableView<ServerSnapshot.TeamRow> teamTable = new TableView<>();

    @Override
    public String title() {
        return "Tournament";
    }

    @Override
    public Node build() {
        Label header = new Label("Tournament Control");
        header.getStyleClass().add("section-header");

        for (Label l : new Label[]{stageLabel, countdownLabel, eventLabel, configLabel, nextConfigLabel}) {
            l.getStyleClass().add("config-line");
        }
        VBox status = new VBox(4, stageLabel, countdownLabel, eventLabel, configLabel, nextConfigLabel);
        status.getStyleClass().add("config-box");

        // Stage control
        Label stageHeader = new Label("Force Stage");
        stageHeader.getStyleClass().add("subsection-header");
        Button lobby = actionBtn("Force Lobby", () -> ActionDispatcher.forceStage(Tournament.StageName.LOBBY));
        Button main = actionBtn("Force Main", () -> ActionDispatcher.forceStage(Tournament.StageName.MAIN));
        Button fin = actionBtn("Force Final", () -> ActionDispatcher.forceStage(Tournament.StageName.FINAL));
        FlowPane stageButtons = new FlowPane(10, 10, lobby, main, fin);

        // Event control
        Label eventHeader = new Label("Live Events");
        eventHeader.getStyleClass().add("subsection-header");
        Button koth = actionBtn("Spawn King of the Hill", () -> ActionDispatcher.spawnLiveEvent(new KingOfTheHill()));
        Button cart = actionBtn("Spawn Gold Cart", () -> ActionDispatcher.spawnLiveEvent(new GoldCart()));
        Button chest = actionBtn("Spawn Static Chest", () -> ActionDispatcher.spawnLiveEvent(new StaticChest()));
        Button breach = actionBtn("Spawn Breach", () -> ActionDispatcher.spawnLiveEvent(new Breach()));
        Button killEvent = actionBtn("Kill Current Event", ActionDispatcher::killCurrentEvent);
        killEvent.getStyleClass().add("danger-button");
        FlowPane eventButtons = new FlowPane(10, 10, koth, cart, chest, breach, killEvent);

        // Standings
        Label teamHeader = new Label("Team Standings");
        teamHeader.getStyleClass().add("subsection-header");
        teamTable.getColumns().add(teamCol("Team", ServerSnapshot.TeamRow::name, 180));
        teamTable.getColumns().add(teamCol("ID", ServerSnapshot.TeamRow::id, 80));
        teamTable.getColumns().add(teamCol("Size", r -> String.valueOf(r.size()), 70));
        teamTable.getColumns().add(teamCol("Kills", r -> String.valueOf(r.kills()), 70));
        teamTable.getColumns().add(teamCol("Deaths", r -> String.valueOf(r.deaths()), 70));
        teamTable.setItems(teams);
        teamTable.setPlaceholder(new Label("No teams registered."));
        VBox.setVgrow(teamTable, Priority.ALWAYS);

        VBox root = new VBox(12, header, status,
                stageHeader, stageButtons,
                eventHeader, eventButtons,
                teamHeader, teamTable);
        root.setPadding(new Insets(18));
        root.getStyleClass().add("tab-root");
        return root;
    }

    private static Button actionBtn(String text, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().add("action-button");
        b.setOnAction(e -> action.run());
        return b;
    }

    private static TableColumn<ServerSnapshot.TeamRow, String> teamCol(
            String title, java.util.function.Function<ServerSnapshot.TeamRow, String> getter, double width) {
        TableColumn<ServerSnapshot.TeamRow, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new SimpleStringProperty(getter.apply(cd.getValue())));
        c.setPrefWidth(width);
        return c;
    }

    @Override
    public void onSnapshot(ServerSnapshot s) {
        stageLabel.setText("Stage: " + s.tournamentStage());
        countdownLabel.setText("Change in: " + s.timeUntilChange());
        eventLabel.setText("Live event: " + s.currentEventName());
        configLabel.setText("Config: " + s.configName());
        nextConfigLabel.setText("Next: " + s.nextConfigName());
        teams.setAll(s.teams());
    }
}
