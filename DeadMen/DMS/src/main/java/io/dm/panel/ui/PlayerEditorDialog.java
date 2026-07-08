package io.dm.panel.ui;

import io.dm.model.stat.StatType;
import io.dm.panel.core.ActionDispatcher;
import io.dm.panel.core.ServerSnapshot;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * A rich, interactive editor for a single online player: edit every skill level,
 * max/restore/reset stats, give items, set point balances, and teleport — all routed
 * through {@link ActionDispatcher} onto the game thread. Opened from {@link PlayersTab}.
 */
final class PlayerEditorDialog {

    private PlayerEditorDialog() {}

    static void open(ServerSnapshot.PlayerRow row) {
        final String name = row.name();

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Player");
        dialog.setResizable(true);

        Label header = new Label(name + "   [" + row.rank() + "]");
        header.getStyleClass().add("section-header");

        VBox content = new VBox(14, header,
                buildStatsSection(name),
                buildItemSection(name),
                buildPointsSection(name, row),
                buildTeleportSection(name, row));
        content.setPadding(new Insets(18));
        content.getStyleClass().add("tab-root");

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefSize(560, 640);
        scroll.getStyleClass().add("tab-root");

        DialogPane pane = dialog.getDialogPane();
        pane.setContent(scroll);
        pane.getButtonTypes().add(ButtonType.CLOSE);
        pane.getStyleClass().add("app-root");
        var css = PlayerEditorDialog.class.getResource("/panel/dark-theme.css");
        if (css != null) {
            pane.getStylesheets().add(css.toExternalForm());
        }
        dialog.showAndWait();
    }

    // --- Stats: per-skill spinner + Set, plus bulk Apply/Max/Restore/Reset -----------

    private static VBox buildStatsSection(String name) {
        Label title = new Label("STATS");
        title.getStyleClass().add("subsection-header");

        StatType[] types = StatType.values();
        Spinner<?>[] spinners = new Spinner<?>[types.length];
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(6);
        int cols = 2;
        for (int i = 0; i < types.length; i++) {
            final int ordinal = i;
            Spinner<Integer> sp = new Spinner<>(1, 99, 99);
            sp.setEditable(true);
            sp.setPrefWidth(72);
            sp.getStyleClass().add("spinner");
            spinners[i] = sp;
            Button set = smallBtn("Set", () -> ActionDispatcher.setStat(name, ordinal, sp.getValue()));
            Label lbl = new Label(types[i].name());
            lbl.setPrefWidth(96);
            HBox rowBox = new HBox(6, lbl, sp, set);
            rowBox.setAlignment(Pos.CENTER_LEFT);
            GridPane.setColumnIndex(rowBox, i % cols);
            GridPane.setRowIndex(rowBox, i / cols);
            grid.getChildren().add(rowBox);
        }

        // Pre-fill spinners with the player's current levels.
        ActionDispatcher.readPlayerLevels(name, levels -> {
            if (levels == null) return;
            for (int i = 0; i < spinners.length && i < levels.length; i++) {
                @SuppressWarnings("unchecked")
                Spinner<Integer> sp = (Spinner<Integer>) spinners[i];
                int lvl = Math.max(1, Math.min(99, levels[i]));
                sp.getValueFactory().setValue(lvl);
            }
        });

        Button applyAll = btn("Apply All", () -> {
            for (int i = 0; i < spinners.length; i++) {
                @SuppressWarnings("unchecked")
                Spinner<Integer> sp = (Spinner<Integer>) spinners[i];
                ActionDispatcher.setStat(name, i, sp.getValue());
            }
        });
        applyAll.getStyleClass().add("primary-button");
        Button max = btn("Max All", () -> ActionDispatcher.maxStats(name));
        Button restore = btn("Restore", () -> ActionDispatcher.restoreStats(name));
        Button reset = btn("Reset", () -> ActionDispatcher.resetStats(name));
        reset.getStyleClass().add("danger-button");
        HBox bulk = new HBox(10, applyAll, max, restore, reset);
        bulk.setAlignment(Pos.CENTER_LEFT);

        return section(title, grid, bulk);
    }

    // --- Visual inventory & bank, with add/remove -------------------------------------

    private static VBox buildItemSection(String name) {
        Label title = new Label("INVENTORY & BANK");
        title.getStyleClass().add("subsection-header");

        VBox invHolder = new VBox();
        VBox bankHolder = new VBox();
        ScrollPane bankScroll = new ScrollPane(bankHolder);
        bankScroll.setFitToWidth(true);
        bankScroll.setPrefViewportHeight(170);
        bankScroll.getStyleClass().add("tab-root");

        // A holder for the refresh closure so nested remove-actions can re-trigger it.
        final Runnable[] refreshRef = new Runnable[1];
        Runnable refresh = () -> ActionDispatcher.readContainers(name, (inv, bank) -> {
            invHolder.getChildren().setAll(ItemGridView.build(name, inv, false, refreshRef[0]));
            bankHolder.getChildren().setAll(ItemGridView.build(name, bank, true, refreshRef[0]));
        });
        refreshRef[0] = refresh;

        Button add = btn("Add Item…", () -> ItemPickerDialog.open(name, refresh));
        add.getStyleClass().add("primary-button");
        Button refreshBtn = btn("Refresh", refresh);
        Label hint = new Label("Right-click an item to remove it.");
        hint.getStyleClass().add("muted-note");
        HBox controls = new HBox(10, add, refreshBtn, hint);
        controls.setAlignment(Pos.CENTER_LEFT);

        Label invLabel = new Label("Inventory");
        invLabel.getStyleClass().add("config-line");
        Label bankLabel = new Label("Bank");
        bankLabel.getStyleClass().add("config-line");

        refresh.run(); // initial load (async, populates on the FX thread)

        VBox invBox = new VBox(5, invLabel, invHolder);
        VBox bankBox = new VBox(5, bankLabel, bankScroll);
        return section(title, controls, invBox, bankBox);
    }

    // --- Point balances ---------------------------------------------------------------

    private static VBox buildPointsSection(String name, ServerSnapshot.PlayerRow row) {
        Label title = new Label("POINT BALANCES");
        title.getStyleClass().add("subsection-header");
        VBox rows = new VBox(6,
                pointRow("Overworld", row.overworldPoints(), v -> ActionDispatcher.setOverworldPoints(name, v)),
                pointRow("Task Vault", row.overworldPointsVault(), v -> ActionDispatcher.setVaultPoints(name, v)),
                pointRow("PvM", row.pvmPoints(), v -> ActionDispatcher.setPvmPoints(name, v)),
                pointRow("Appreciation", row.appreciationPoints(), v -> ActionDispatcher.setAppreciationPoints(name, v)));
        return section(title, rows);
    }

    private static HBox pointRow(String label, int current, java.util.function.IntConsumer setter) {
        Label l = new Label(label);
        l.setPrefWidth(110);
        Spinner<Integer> sp = intSpinner(0, Integer.MAX_VALUE, current, 100, 140);
        Button set = btn("Set", () -> setter.accept(sp.getValue()));
        HBox box = new HBox(10, l, sp, set);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    // --- Teleport ---------------------------------------------------------------------

    private static VBox buildTeleportSection(String name, ServerSnapshot.PlayerRow row) {
        Label title = new Label("TELEPORT");
        title.getStyleClass().add("subsection-header");
        int[] pos = parsePosition(row.position());
        Spinner<Integer> x = intSpinner(0, 16384, pos[0], 1, 90);
        Spinner<Integer> y = intSpinner(0, 16384, pos[1], 1, 90);
        Spinner<Integer> z = intSpinner(0, 3, pos[2], 1, 70);
        Button go = btn("Teleport", () -> ActionDispatcher.teleport(name, x.getValue(), y.getValue(), z.getValue()));
        go.getStyleClass().add("primary-button");
        HBox bar = new HBox(10, new Label("X"), x, new Label("Y"), y, new Label("Z"), z, go);
        bar.setAlignment(Pos.CENTER_LEFT);
        return section(title, bar);
    }

    private static int[] parsePosition(String pos) {
        int[] out = {3222, 3218, 0};
        try {
            String[] parts = pos.split(",");
            for (int i = 0; i < 3 && i < parts.length; i++) out[i] = Integer.parseInt(parts[i].trim());
        } catch (Throwable ignored) {}
        return out;
    }

    // --- helpers ----------------------------------------------------------------------

    private static VBox section(Node... children) {
        VBox box = new VBox(10, children);
        box.getStyleClass().add("config-box");
        box.setPadding(new Insets(12));
        return box;
    }

    private static Spinner<Integer> intSpinner(int min, int max, int initial, int step, double width) {
        Spinner<Integer> s = new Spinner<>(min, max, initial, step);
        s.setEditable(true);
        s.setPrefWidth(width);
        s.getStyleClass().add("spinner");
        return s;
    }

    private static Button btn(String text, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().add("action-button");
        b.setOnAction(e -> action.run());
        return b;
    }

    private static Button smallBtn(String text, Runnable action) {
        Button b = btn(text, action);
        b.setStyle("-fx-padding: 4 10 4 10; -fx-font-size: 11px;");
        return b;
    }
}
