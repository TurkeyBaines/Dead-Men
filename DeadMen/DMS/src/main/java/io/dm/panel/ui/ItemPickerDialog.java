package io.dm.panel.ui;

import io.dm.cache.ItemDef;
import io.dm.panel.core.ActionDispatcher;
import io.dm.panel.core.ItemIconService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Searchable item picker: every named item in the cache ({@link ItemDef#cached}),
 * shown with its icon, searchable by <b>ID or name</b>. Pick an amount and add the
 * item to the target player's inventory or bank via {@link ActionDispatcher}.
 *
 * <p>The list is virtualized, so icons load lazily only for visible rows.
 */
final class ItemPickerDialog {

    private static List<Integer> allIdsCache;

    private ItemPickerDialog() {}

    static void open(String player, Runnable onChanged) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Item — " + player);
        dialog.setResizable(true);

        TextField search = new TextField();
        search.setPromptText("Search by name or item ID…");
        search.getStyleClass().add("search-field");

        ObservableList<Integer> master = FXCollections.observableArrayList(allItemIds());
        FilteredList<Integer> filtered = new FilteredList<>(master, i -> true);
        ListView<Integer> list = new ListView<>(filtered);
        list.setCellFactory(lv -> new ItemCell());
        list.setPrefHeight(430);
        VBox.setVgrow(list, Priority.ALWAYS);

        Label count = new Label();
        Runnable updateCount = () -> count.setText(filtered.size() + " item(s)");
        updateCount.run();

        search.textProperty().addListener((o, a, q) -> {
            String query = q == null ? "" : q.trim().toLowerCase();
            filtered.setPredicate(id -> {
                if (query.isEmpty()) return true;
                if (String.valueOf(id).contains(query)) return true;
                return ItemIconService.nameOf(id).toLowerCase().contains(query);
            });
            updateCount.run();
        });

        Spinner<Integer> amount = new Spinner<>(1, Integer.MAX_VALUE, 1, 1);
        amount.setEditable(true);
        amount.setPrefWidth(130);
        amount.getStyleClass().add("spinner");

        Button addInv = new Button("Add to Inventory");
        addInv.getStyleClass().addAll("action-button", "primary-button");
        Button addBank = new Button("Add to Bank");
        addBank.getStyleClass().add("action-button");
        addInv.disableProperty().bind(list.getSelectionModel().selectedItemProperty().isNull());
        addBank.disableProperty().bind(list.getSelectionModel().selectedItemProperty().isNull());
        addInv.setOnAction(e -> add(list, amount, false, player, onChanged));
        addBank.setOnAction(e -> add(list, amount, true, player, onChanged));
        list.setOnMouseClicked(e -> { if (e.getClickCount() == 2) add(list, amount, false, player, onChanged); });

        HBox actionBar = new HBox(10, new Label("Amount"), amount, addInv, addBank);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        HBox topBar = new HBox(10, search, count);
        topBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(search, Priority.ALWAYS);

        String iconNote = ItemIconService.hasLocalIcons()
                ? "Icons: local cache folder."
                : "Icons: RuneLite CDN (needs internet); styled tiles otherwise.";
        Label note = new Label(iconNote);
        note.getStyleClass().add("muted-note");

        VBox root = new VBox(10, topBar, list, actionBar, note);
        root.setPadding(new Insets(16));
        root.getStyleClass().add("tab-root");
        root.setPrefSize(480, 600);

        DialogPane pane = dialog.getDialogPane();
        pane.setContent(root);
        pane.getButtonTypes().add(ButtonType.CLOSE);
        pane.getStyleClass().add("app-root");
        var css = ItemPickerDialog.class.getResource("/panel/dark-theme.css");
        if (css != null) pane.getStylesheets().add(css.toExternalForm());
        dialog.showAndWait();
    }

    private static void add(ListView<Integer> list, Spinner<Integer> amount, boolean bank, String player, Runnable onChanged) {
        Integer id = list.getSelectionModel().getSelectedItem();
        if (id == null) return;
        if (bank) {
            ActionDispatcher.addToBank(player, id, amount.getValue());
        } else {
            ActionDispatcher.giveItem(player, id, amount.getValue());
        }
        if (onChanged != null) {
            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(250));
            delay.setOnFinished(ev -> onChanged.run());
            delay.play();
        }
    }

    /** All item IDs that have a real name, sorted ascending. Cached after first build. */
    private static synchronized List<Integer> allItemIds() {
        if (allIdsCache != null) return allIdsCache;
        List<Integer> ids = new ArrayList<>();
        try {
            for (Map.Entry<Integer, ItemDef> e : ItemDef.cached.entrySet()) {
                ItemDef def = e.getValue();
                if (def == null) continue;
                String n = def.name;
                if (n == null || n.isBlank() || n.equalsIgnoreCase("null")) continue;
                ids.add(e.getKey());
            }
            ids.sort(Comparator.naturalOrder());
        } catch (Throwable ignored) {}
        allIdsCache = ids;
        return ids;
    }

    /** One picker row: icon + "name  (id N)". */
    private static final class ItemCell extends ListCell<Integer> {
        @Override
        protected void updateItem(Integer id, boolean empty) {
            super.updateItem(id, empty);
            if (empty || id == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            StackPane icon = ItemIconService.tile(id, 1, 28);
            Label label = new Label(ItemIconService.nameOf(id) + "   (id " + id + ")");
            HBox box = new HBox(10, icon, label);
            box.setAlignment(Pos.CENTER_LEFT);
            setGraphic(box);
            setText(null);
        }
    }
}
