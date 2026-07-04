package io.dm.panel.ui;

import io.dm.panel.core.ActionDispatcher;
import io.dm.panel.core.ServerSnapshot;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Overworld economy view: current season code, a points leaderboard for online players
 * (sortable by any points column), and a manual Overworld-points grant tool.
 */
public final class EconomyTab implements PanelTab {

    private final Label seasonLabel = new Label("Tournament Code: --");
    private final ObservableList<ServerSnapshot.PlayerRow> rows = FXCollections.observableArrayList();
    private final TableView<ServerSnapshot.PlayerRow> table = new TableView<>();

    private final ComboBox<String> grantTarget = new ComboBox<>();
    private final Spinner<Integer> grantAmount = new Spinner<>(-1_000_000, 1_000_000_000, 1000, 1000);

    @Override
    public String title() {
        return "Economy";
    }

    @Override
    public Node build() {
        Label header = new Label("Overworld Economy");
        header.getStyleClass().add("section-header");
        seasonLabel.getStyleClass().add("config-line");

        TableColumn<ServerSnapshot.PlayerRow, String> nameCol = strCol("Player", r -> r.name());
        nameCol.setPrefWidth(160);
        TableColumn<ServerSnapshot.PlayerRow, Number> owCol = numCol("Overworld", r -> r.overworldPoints());
        TableColumn<ServerSnapshot.PlayerRow, Number> vaultCol = numCol("Task Vault", r -> r.overworldPointsVault());
        TableColumn<ServerSnapshot.PlayerRow, Number> pvmCol = numCol("PvM", r -> r.pvmPoints());
        TableColumn<ServerSnapshot.PlayerRow, Number> apprCol = numCol("Appreciation", r -> r.appreciationPoints());
        table.getColumns().addAll(nameCol, owCol, vaultCol, pvmCol, apprCol);
        table.setItems(rows);
        table.setPlaceholder(new Label("No players online."));
        // Default sort: highest Overworld points first.
        owCol.setSortType(TableColumn.SortType.DESCENDING);
        table.getSortOrder().add(owCol);
        VBox.setVgrow(table, Priority.ALWAYS);

        // Grant tool
        Label grantHeader = new Label("Grant Overworld Points");
        grantHeader.getStyleClass().add("subsection-header");
        grantTarget.setPromptText("Select online player…");
        grantTarget.setPrefWidth(200);
        grantAmount.setEditable(true);
        grantAmount.getStyleClass().add("spinner");
        Button grant = new Button("Grant");
        grant.getStyleClass().add("action-button");
        grant.setOnAction(e -> {
            String target = grantTarget.getValue();
            if (target == null || target.isBlank()) {
                return;
            }
            ActionDispatcher.grantOverworldPoints(target, grantAmount.getValue());
        });
        HBox grantBar = new HBox(10, grantTarget, grantAmount, grant);
        grantBar.setAlignment(Pos.CENTER_LEFT);
        grantBar.getStyleClass().add("action-bar");

        VBox root = new VBox(12, header, seasonLabel, table, grantHeader, grantBar);
        root.setPadding(new Insets(18));
        root.getStyleClass().add("tab-root");
        return root;
    }

    private static TableColumn<ServerSnapshot.PlayerRow, String> strCol(
            String title, java.util.function.Function<ServerSnapshot.PlayerRow, String> getter) {
        TableColumn<ServerSnapshot.PlayerRow, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new SimpleStringProperty(getter.apply(cd.getValue())));
        return c;
    }

    private static TableColumn<ServerSnapshot.PlayerRow, Number> numCol(
            String title, java.util.function.ToIntFunction<ServerSnapshot.PlayerRow> getter) {
        TableColumn<ServerSnapshot.PlayerRow, Number> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new SimpleIntegerProperty(getter.applyAsInt(cd.getValue())));
        c.setPrefWidth(120);
        return c;
    }

    @Override
    public void onSnapshot(ServerSnapshot s) {
        seasonLabel.setText("Tournament Code: " + s.seasonCode());
        rows.setAll(s.onlinePlayers());
        // Keep the current sort applied after a data refresh.
        if (!table.getSortOrder().isEmpty()) {
            table.sort();
        }
        // Refresh grant target choices, preserving the current selection if still online.
        String selected = grantTarget.getValue();
        ObservableList<String> names = FXCollections.observableArrayList();
        for (ServerSnapshot.PlayerRow r : s.onlinePlayers()) {
            names.add(r.name());
        }
        grantTarget.setItems(names);
        if (selected != null && names.contains(selected)) {
            grantTarget.setValue(selected);
        }
    }
}
