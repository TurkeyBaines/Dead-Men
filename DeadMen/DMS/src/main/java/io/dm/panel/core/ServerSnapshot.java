package io.dm.panel.core;

import java.util.List;

/**
 * Immutable point-in-time snapshot of everything the panel's read-only views render.
 *
 * <p>Produced once per poll by {@link SnapshotService} off the game thread, then handed
 * to the JavaFX thread via {@code Platform.runLater}. Decouples "reading live game state"
 * from "rendering," so no UI code ever iterates {@code World.players} on the FX thread.
 */
public record ServerSnapshot(
        // --- server health (from Server.worker / ProcessWorker) ---
        long uptimeMs,
        long tickCount,
        long tickPeriodMs,
        long slowExecutions,
        long lastExecutionMs,
        long slowestExecutionMs,
        long queuedTasks,
        // --- population ---
        int playersOnline,
        int overworldPopulation,
        int citadelPopulation,
        // --- world / tournament ---
        String worldName,
        int worldId,
        String worldStage,
        String worldType,
        String tournamentStage,
        String timeUntilChange,
        String seasonCode,
        String configName,
        String nextConfigName,
        String currentEventName,
        // --- tables ---
        List<PlayerRow> onlinePlayers,
        List<TeamRow> teams
) {

    /** One row in the players / economy tables. */
    public record PlayerRow(
            String name,
            String rank,
            boolean staff,
            String position,
            int overworldPoints,
            int overworldPointsVault,
            int pvmPoints,
            int appreciationPoints,
            long playTimeTicks
    ) {}

    /** One row in the tournament team-standings table. */
    public record TeamRow(String id, String name, int size, int kills, int deaths) {}
}
