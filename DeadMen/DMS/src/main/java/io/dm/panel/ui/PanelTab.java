package io.dm.panel.ui;

import io.dm.panel.core.ServerSnapshot;
import javafx.scene.Node;

/**
 * Common contract for every tab in the Server Panel so the shell can wire them
 * uniformly (title + root node + optional per-poll refresh hook).
 */
public interface PanelTab {

    /** Tab label. */
    String title();

    /** Build and return the tab's root node (called once). */
    Node build();

    /**
     * Called on the JavaFX thread once per snapshot poll (~1s). Tabs that render live
     * state override this; static tabs (e.g. the console, which is push-driven) ignore it.
     */
    default void onSnapshot(ServerSnapshot snapshot) {}
}
