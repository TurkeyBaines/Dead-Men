package io.dm.panel.ui;

import io.dm.panel.core.ActionDispatcher;
import io.dm.panel.core.ItemIconService;
import io.dm.panel.core.ItemStack;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;

import java.util.List;

/**
 * A visual grid of item icon tiles (see {@link ItemIconService}) for a player's
 * inventory or bank. Right-click a tile to remove items via {@link ActionDispatcher}.
 */
final class ItemGridView {

    static final double TILE = 38;

    private ItemGridView() {}

    static FlowPane build(String player, List<ItemStack> items, boolean fromBank, Runnable refresh) {
        FlowPane pane = new FlowPane(4, 4);
        pane.getStyleClass().add("item-grid");
        if (items == null || items.isEmpty()) {
            Label empty = new Label(fromBank ? "Bank is empty." : "Inventory is empty.");
            empty.getStyleClass().add("muted-note");
            pane.getChildren().add(empty);
            return pane;
        }
        for (ItemStack stack : items) {
            StackPane tile = ItemIconService.tile(stack.id(), stack.amount(), TILE);

            ContextMenu menu = new ContextMenu();
            menu.getItems().add(header(ItemIconService.nameOf(stack.id()) + "  (x" + stack.amount() + ")"));
            menu.getItems().add(new SeparatorMenuItem());
            menu.getItems().add(item("Remove 1", () -> remove(player, stack.id(), 1, fromBank, refresh)));
            if (stack.amount() > 1) {
                menu.getItems().add(item("Remove 10", () -> remove(player, stack.id(), 10, fromBank, refresh)));
                menu.getItems().add(item("Remove all (" + stack.amount() + ")",
                        () -> remove(player, stack.id(), stack.amount(), fromBank, refresh)));
            }
            tile.setOnContextMenuRequested(e -> menu.show(tile, e.getScreenX(), e.getScreenY()));
            tile.setOnMouseClicked(e -> { if (e.getButton().name().equals("PRIMARY")) menu.show(tile, e.getScreenX(), e.getScreenY()); });
            pane.getChildren().add(tile);
        }
        return pane;
    }

    private static void remove(String player, int id, int amount, boolean fromBank, Runnable refresh) {
        ActionDispatcher.removeItem(player, id, amount, fromBank);
        // Give the game thread a beat to apply, then reload the grids.
        if (refresh != null) {
            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(250));
            delay.setOnFinished(ev -> refresh.run());
            delay.play();
        }
    }

    private static MenuItem item(String text, Runnable action) {
        MenuItem mi = new MenuItem(text);
        mi.setOnAction(e -> action.run());
        return mi;
    }

    private static MenuItem header(String text) {
        MenuItem mi = new MenuItem(text);
        mi.setDisable(true);
        return mi;
    }
}
