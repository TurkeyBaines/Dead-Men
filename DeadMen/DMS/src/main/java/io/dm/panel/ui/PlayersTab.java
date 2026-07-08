package io.dm.panel.ui;

import io.dm.panel.core.ActionDispatcher;
import io.dm.panel.core.PanelFormat;
import io.dm.panel.core.ServerSnapshot;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Online players table with live search and one-click moderation (kick / mute / ban).
 * Moderation is routed through {@link ActionDispatcher}, which acts as the resolved
 * online staff member and runs on the game thread.
 */
public final class PlayersTab implements PanelTab {

    private final ObservableList<ServerSnapshot.PlayerRow> master = FXCollections.observableArrayList();
    private final TextField search = new TextField();
    private final TableView<ServerSnapshot.PlayerRow> table = new TableView<>();
    private final Label count = new Label("0 online");

    @Override
    public String title() {
        return "Players";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Node build() {
        Label header = new Label("Online Players");
        header.getStyleClass().add("section-header");

        search.setPromptText("Search by name or rank…");
        search.getStyleClass().add("search-field");
        HBox.setHgrow(search, Priority.ALWAYS);

        HBox topBar = new HBox(10, search, count);
        topBar.setAlignment(Pos.CENTER_LEFT);

        TableColumn<ServerSnapshot.PlayerRow, String> nameCol = col("Name", r -> r.name());
        TableColumn<ServerSnapshot.PlayerRow, String> rankCol = col("Rank", r -> r.rank());
        TableColumn<ServerSnapshot.PlayerRow, String> posCol = col("Position", r -> r.position());
        TableColumn<ServerSnapshot.PlayerRow, String> owCol = col("Overworld Pts", r -> PanelFormat.number(r.overworldPoints()));
        TableColumn<ServerSnapshot.PlayerRow, String> timeCol = col("Playtime", r -> PanelFormat.playTime(r.playTimeTicks()));
        nameCol.setPrefWidth(160);
        rankCol.setPrefWidth(150);
        posCol.setPrefWidth(120);
        owCol.setPrefWidth(120);
        timeCol.setPrefWidth(140);
        table.getColumns().addAll(nameCol, rankCol, posCol, owCol, timeCol);
        table.setPlaceholder(new Label("No players online."));
        VBox.setVgrow(table, Priority.ALWAYS);

        FilteredList<ServerSnapshot.PlayerRow> filtered = new FilteredList<>(master, r -> true);
        search.textProperty().addListener((obs, old, val) -> {
            String q = val == null ? "" : val.trim().toLowerCase();
            filtered.setPredicate(r -> q.isEmpty()
                    || r.name().toLowerCase().contains(q)
                    || r.rank().toLowerCase().contains(q));
        });
        SortedList<ServerSnapshot.PlayerRow> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);

        Button edit = new Button("Edit Player");
        Button details = new Button("View Details");
        Button kick = new Button("Kick");
        Button mute = new Button("Mute 1h");
        Button ban = new Button("Ban");
        edit.getStyleClass().addAll("action-button", "primary-button");
        details.getStyleClass().add("action-button");
        kick.getStyleClass().add("action-button");
        mute.getStyleClass().add("action-button");
        ban.getStyleClass().addAll("action-button", "danger-button");

        edit.setDisable(true);
        details.setDisable(true);
        kick.setDisable(true);
        mute.setDisable(true);
        ban.setDisable(true);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            boolean none = sel == null;
            edit.setDisable(none);
            details.setDisable(none);
            kick.setDisable(none);
            mute.setDisable(none);
            ban.setDisable(none);
        });

        edit.setOnAction(e -> withSelection(PlayerEditorDialog::open));
        details.setOnAction(e -> withSelection(row ->
                ActionDispatcher.inspectPlayer(row.name(), text -> showDetails(row.name(), text))));
        kick.setOnAction(e -> withSelection(row -> confirm("Kick " + row.name() + "?",
                () -> ActionDispatcher.kick(row.name()))));
        mute.setOnAction(e -> withSelection(row -> confirm("Mute " + row.name() + " for 1 hour?",
                () -> ActionDispatcher.mute(row.name(), 3600_000L, false))));
        ban.setOnAction(e -> withSelection(row -> confirm("BAN " + row.name() + "? This forces logout.",
                () -> ActionDispatcher.ban(row.name()))));

        // Double-click opens the editor; right-click offers a quick-action menu.
        table.setRowFactory(tv -> {
            javafx.scene.control.TableRow<ServerSnapshot.PlayerRow> r = new javafx.scene.control.TableRow<>();
            MenuItem miEdit = menuItem("Edit Player…", () -> PlayerEditorDialog.open(r.getItem()));
            MenuItem miDetails = menuItem("View Details…",
                    () -> ActionDispatcher.inspectPlayer(r.getItem().name(), t -> showDetails(r.getItem().name(), t)));
            MenuItem miKick = menuItem("Kick",
                    () -> confirm("Kick " + r.getItem().name() + "?", () -> ActionDispatcher.kick(r.getItem().name())));
            MenuItem miBan = menuItem("Ban",
                    () -> confirm("BAN " + r.getItem().name() + "?", () -> ActionDispatcher.ban(r.getItem().name())));
            ContextMenu menu = new ContextMenu(miEdit, miDetails, new SeparatorMenuItem(), miKick, miBan);
            r.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(r.emptyProperty()).then((ContextMenu) null).otherwise(menu));
            r.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !r.isEmpty()) {
                    PlayerEditorDialog.open(r.getItem());
                }
            });
            return r;
        });

        HBox actions = new HBox(10, edit, details, kick, mute, ban);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.getStyleClass().add("action-bar");

        VBox root = new VBox(12, header, topBar, table, actions);
        root.setPadding(new Insets(18));
        root.getStyleClass().add("tab-root");
        return root;
    }

    private void withSelection(java.util.function.Consumer<ServerSnapshot.PlayerRow> action) {
        ServerSnapshot.PlayerRow sel = table.getSelectionModel().getSelectedItem();
        if (sel != null) {
            action.accept(sel);
        }
    }

    private static MenuItem menuItem(String text, Runnable action) {
        MenuItem mi = new MenuItem(text);
        mi.setOnAction(e -> action.run());
        return mi;
    }

    private void showDetails(String name, String text) {
        TextArea area = new TextArea(text);
        area.setEditable(false);
        area.setWrapText(false);
        area.getStyleClass().add("command-field");
        area.setPrefSize(460, 520);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Player Details");
        alert.setHeaderText(name);
        alert.getDialogPane().setContent(area);
        alert.getDialogPane().setPrefWidth(500);
        alert.setResizable(true);
        alert.showAndWait();
    }

    private void confirm(String message, Runnable onYes) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.setTitle("Confirm action");
        alert.showAndWait().filter(bt -> bt == ButtonType.YES).ifPresent(bt -> onYes.run());
    }

    private static TableColumn<ServerSnapshot.PlayerRow, String> col(
            String title, java.util.function.Function<ServerSnapshot.PlayerRow, String> getter) {
        TableColumn<ServerSnapshot.PlayerRow, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(getter.apply(cd.getValue())));
        return c;
    }

    @Override
    public void onSnapshot(ServerSnapshot s) {
        // Preserve selection by name across the wholesale refresh.
        ServerSnapshot.PlayerRow sel = table.getSelectionModel().getSelectedItem();
        String selName = sel == null ? null : sel.name();
        master.setAll(s.onlinePlayers());
        count.setText(s.playersOnline() + " online");
        if (selName != null) {
            for (ServerSnapshot.PlayerRow r : table.getItems()) {
                if (r.name().equals(selName)) {
                    table.getSelectionModel().select(r);
                    break;
                }
            }
        }
    }
}
