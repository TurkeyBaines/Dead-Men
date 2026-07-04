package io.dm.panel;

import io.dm.panel.core.ConsoleTeeStream;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Single entry point for booting the in-process Server Panel, called from
 * {@code io.dm.Server.main()} on a dedicated thread so it never blocks server startup
 * or Netty.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Install the {@link ConsoleTeeStream} so the panel captures all stdout/stderr
 *       (which, under the slf4j-simple binding, also carries SLF4J log output).</li>
 *   <li>Initialize the JavaFX toolkit via {@link Platform#startup} (non-blocking, unlike
 *       {@code Application.launch}).</li>
 *   <li>Disable implicit exit so closing the window never terminates the JVM/server.</li>
 * </ul>
 *
 * <p>Any failure here is swallowed and logged: the panel is an operator convenience and
 * must never take the game server down with it.
 */
public final class ServerPanelLauncher {

    private ServerPanelLauncher() {}

    public static void launch() {
        try {
            // Capture output as early as possible, before the UI toolkit spins up.
            ConsoleTeeStream.install();

            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                showWindow();
            });
        } catch (IllegalStateException alreadyStarted) {
            // Toolkit was already initialized — just show the window on the FX thread.
            Platform.runLater(ServerPanelLauncher::showWindow);
        } catch (Throwable t) {
            System.err.println("[ServerPanel] Failed to launch panel: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private static void showWindow() {
        try {
            new ServerPanelApp().start(new Stage());
        } catch (Throwable t) {
            System.err.println("[ServerPanel] Failed to build panel window: " + t.getMessage());
            t.printStackTrace();
        }
    }
}
