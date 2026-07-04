package io.dm.panel.ui;

import io.dm.panel.core.ActionDispatcher;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Curated one-click operator actions for the most common tasks: world broadcast,
 * double-reward toggles, timed system update, and a guarded server shutdown.
 * Everything routes through {@link ActionDispatcher} onto the game thread.
 */
public final class QuickActionsTab implements PanelTab {

    @Override
    public String title() {
        return "Quick Actions";
    }

    @Override
    public Node build() {
        Label header = new Label("Quick Actions");
        header.getStyleClass().add("section-header");

        // Broadcast
        Label bcHeader = new Label("Broadcast Message");
        bcHeader.getStyleClass().add("subsection-header");
        TextField bcField = new TextField();
        bcField.setPromptText("Message to all online players…");
        bcField.setPrefWidth(360);
        Button bcSend = actionBtn("Send", () -> {
            String m = bcField.getText();
            if (m != null && !m.isBlank()) {
                ActionDispatcher.broadcast(m);
                bcField.clear();
            }
        });
        bcSend.getStyleClass().add("primary-button");
        HBox bcBar = new HBox(10, bcField, bcSend);
        bcBar.setAlignment(Pos.CENTER_LEFT);

        // Supply chest broadcast
        TextField supplyField = new TextField();
        supplyField.setPromptText("Supply-chest style news broadcast…");
        supplyField.setPrefWidth(360);
        Button supplySend = actionBtn("Send News", () -> {
            String m = supplyField.getText();
            if (m != null && !m.isBlank()) {
                ActionDispatcher.supplyChestBroadcast(m);
                supplyField.clear();
            }
        });
        HBox supplyBar = new HBox(10, supplyField, supplySend);
        supplyBar.setAlignment(Pos.CENTER_LEFT);

        // Admin utilities
        Label toggleHeader = new Label("Admin Utilities");
        toggleHeader.getStyleClass().add("subsection-header");
        FlowPane toggles = new FlowPane(10, 10,
                actionBtn("Save All Players", () -> confirm("Save all online players now?",
                        ActionDispatcher::saveAllPlayers)),
                actionBtn("Reload Mutes", ActionDispatcher::reloadMutes),
                dangerBtn("Remove All Players", () -> confirm(
                        "Force-log-out ALL online players? (They are saved on logout.)",
                        ActionDispatcher::removeAllPlayers)));

        // System update
        Label updHeader = new Label("System Update");
        updHeader.getStyleClass().add("subsection-header");
        Spinner<Integer> minutes = new Spinner<>(0, 240, 5, 1);
        minutes.setEditable(true);
        minutes.setPrefWidth(100);
        Button startUpdate = actionBtn("Start Update", () ->
                confirm("Start a " + minutes.getValue() + "-minute system update countdown?",
                        () -> ActionDispatcher.systemUpdate(minutes.getValue())));
        Button cancelUpdate = actionBtn("Cancel Update", () -> ActionDispatcher.systemUpdate(0));
        HBox updBar = new HBox(10, new Label("Minutes:"), minutes, startUpdate, cancelUpdate);
        updBar.setAlignment(Pos.CENTER_LEFT);

        // Danger zone
        Label dangerHeader = new Label("Danger Zone");
        dangerHeader.getStyleClass().add("subsection-header");
        Button shutdown = actionBtn("Shutdown Server", () ->
                confirm("Shut the server DOWN now? Players will be saved and disconnected.",
                        ActionDispatcher::shutdownServer));
        shutdown.getStyleClass().add("danger-button");
        Label shutdownNote = new Label("Runs the graceful shutdown hook (saves & logs out players).");
        shutdownNote.getStyleClass().add("muted-note");
        HBox dangerBar = new HBox(10, shutdown, shutdownNote);
        dangerBar.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(12, header,
                bcHeader, bcBar, supplyBar,
                toggleHeader, toggles,
                updHeader, updBar,
                dangerHeader, dangerBar);
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

    private static Button dangerBtn(String text, Runnable action) {
        Button b = actionBtn(text, action);
        b.getStyleClass().add("danger-button");
        return b;
    }

    private static void confirm(String message, Runnable onYes) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.setTitle("Confirm action");
        alert.showAndWait().filter(bt -> bt == ButtonType.YES).ifPresent(bt -> onYes.run());
    }
}
