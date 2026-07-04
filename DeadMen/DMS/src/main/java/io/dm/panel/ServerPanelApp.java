package io.dm.panel;

import io.dm.panel.core.SnapshotService;
import io.dm.panel.ui.*;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * The Server Panel window: a dark {@link TabPane} shell hosting every {@link PanelTab},
 * each subscribed to {@link SnapshotService} for ~1s live refresh.
 *
 * <p>Not a real {@code javafx.application.Application} (we don't call
 * {@code Application.launch}); it's constructed directly from
 * {@link ServerPanelLauncher} on the JavaFX thread.
 */
public final class ServerPanelApp {

    public void start(Stage stage) {
        List<PanelTab> tabs = List.of(
                new DashboardTab(),
                new PlayersTab(),
                new TournamentTab(),
                new EconomyTab(),
                new ConfigTab(),
                new ConsoleTab(),
                new QuickActionsTab()
        );

        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("panel-tabs");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        for (PanelTab panelTab : tabs) {
            Tab tab = new Tab(panelTab.title(), panelTab.build());
            tabPane.getTabs().add(tab);
            SnapshotService.INSTANCE.subscribe(snapshot -> {
                try {
                    panelTab.onSnapshot(snapshot);
                } catch (Throwable ignored) {
                    // A misbehaving tab must not break the poll broadcast to others.
                }
            });
        }

        BorderPane rootPane = new BorderPane(tabPane);
        rootPane.getStyleClass().add("app-root");
        rootPane.setTop(buildTitleBar());

        Scene scene = new Scene(rootPane, 1180, 760);
        var css = ServerPanelApp.class.getResource("/panel/dark-theme.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setTitle("Dead-Men · Server Panel");
        stage.setScene(scene);
        // Closing the window must NOT kill the server — just hide the panel.
        stage.setOnCloseRequest(e -> {
            e.consume();
            stage.hide();
        });

        SnapshotService.INSTANCE.start();
        stage.show();
        System.out.println("[ServerPanel] Panel window ready with " + tabs.size() + " tabs.");
    }

    private HBox buildTitleBar() {
        Label title = new Label("DEAD-MEN");
        title.getStyleClass().add("app-title");
        Label subtitle = new Label("Deadman Tournament · Server Panel");
        subtitle.getStyleClass().add("app-subtitle");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label live = new Label("● LIVE");
        live.getStyleClass().add("live-badge");

        HBox bar = new HBox(12, title, subtitle, spacer, live);
        bar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        bar.getStyleClass().add("title-bar");
        return bar;
    }
}
