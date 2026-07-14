package io.dm.panel.core;

import io.dm.Server;
import io.dm.deadman.Deadman;
import io.dm.deadman.tournament.TournamentConfig;
import io.dm.deadman.tournament.stages.Main;
import io.dm.deadman.tournament.team.Group;
import io.dm.model.World;
import io.dm.model.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Polls live game state once per second on a dedicated background thread, builds an
 * immutable {@link ServerSnapshot}, and publishes it to subscribers on the JavaFX
 * Application Thread via {@code Platform.runLater}.
 *
 * <p>Reads here are best-effort and display-only: they are not posted to the game
 * thread (matching how existing code such as {@code Loggers} reads {@code Player}
 * fields off-thread), so the dashboard tolerates up to one poll interval of lag. Every
 * read is wrapped defensively so a transient game-state race can never kill the poller.
 */
public final class SnapshotService {

    public static final SnapshotService INSTANCE = new SnapshotService();

    private static final long POLL_MS = 1000L;

    private final List<Consumer<ServerSnapshot>> listeners = new CopyOnWriteArrayList<>();
    private ScheduledExecutorService scheduler;

    private SnapshotService() {}

    public synchronized void start() {
        if (scheduler != null) {
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "panel-snapshot-poller");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::poll, 0L, POLL_MS, TimeUnit.MILLISECONDS);
    }

    public synchronized void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    public void subscribe(Consumer<ServerSnapshot> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void unsubscribe(Consumer<ServerSnapshot> listener) {
        listeners.remove(listener);
    }

    private void poll() {
        ServerSnapshot snapshot;
        try {
            snapshot = buildSnapshot();
        } catch (Throwable t) {
            // Never let a bad poll kill the scheduler.
            return;
        }
        // Marshal to the FX thread. Import kept local to avoid a hard JavaFX dependency
        // in the header for readers scanning the game-state reads above.
        javafx.application.Platform.runLater(() -> {
            for (Consumer<ServerSnapshot> listener : listeners) {
                try {
                    listener.accept(snapshot);
                } catch (Throwable ignored) {
                }
            }
        });
    }

    private ServerSnapshot buildSnapshot() {
        long period = Server.worker.getPeriod();
        long executions = Server.worker.getExecutions();

        // Population + world/tournament state (all defensively read).
        int overworldPop = safeCount(() -> Deadman.getCitadel().count());
        int citadelPop = safeCount(() -> Deadman.getCitadel().count());

        String stageName = "UNKNOWN";
        String timeUntilChange = "--:--:--";
        String currentEventName = "None";
        try {
            if (Deadman.getStage() != null) {
                stageName = Deadman.getStage().stageName().text;
                if (Deadman.getStage() instanceof Main main && main.currentEvent != null) {
                    currentEventName = main.currentEvent.getClass().getSimpleName();
                }
            }
            timeUntilChange = Deadman.timeUntilChange();
        } catch (Throwable ignored) {
        }

        String season = safeString(Deadman::getUtc, "n/a");
        String configName = describeConfig(safe(Deadman::getConfig));
        String nextConfigName;
        try {
            nextConfigName = Deadman.canOverrideConfig()
                    ? "Random (open to override)"
                    : Deadman.getNext_Config_Name() + " → " + describeConfig(Deadman.getNext_config());
        } catch (Throwable t) {
            nextConfigName = "n/a";
        }

        // Players + economy rows.
        List<ServerSnapshot.PlayerRow> playerRows = new ArrayList<>();
        int online = 0;
        try {
            for (Player p : World.players) {
                if (p == null) {
                    continue;
                }
                online++;
                playerRows.add(new ServerSnapshot.PlayerRow(
                        safeString(p::getName, "?"),
                        rankOf(p),
                        safeBool(p::isStaff),
                        positionOf(p),
                        0,
                        0,
                        p.PvmPoints,
                        p.appreciationPoints,
                        p.playTime
                ));
            }
        } catch (Throwable ignored) {
        }

        // Team standings.
        List<ServerSnapshot.TeamRow> teamRows = new ArrayList<>();
        try {
            for (Group g : Deadman.getGroups().getTeamList().values()) {
                if (g == null) {
                    continue;
                }
                teamRows.add(new ServerSnapshot.TeamRow(
                        g.getID(), g.getName(), g.size(), g.getKills(), g.getDeaths()));
            }
        } catch (Throwable ignored) {
        }

        return new ServerSnapshot(
                executions * period,
                executions,
                period,
                Server.worker.getSlowExecutions(),
                Server.worker.getLastExecutionTime(),
                Server.worker.getSlowestExecutionTime(),
                Server.worker.getQueuedTaskCount(),
                online,
                overworldPop,
                citadelPop,
                World.name,
                World.id,
                safeString(() -> World.stage.name(), "?"),
                safeString(() -> World.type.name(), "?"),
                stageName,
                timeUntilChange,
                season,
                configName,
                nextConfigName,
                currentEventName,
                playerRows,
                teamRows
        );
    }

    private static String describeConfig(TournamentConfig config) {
        if (config == null) {
            return "n/a";
        }
        try {
            String mutator = config.MUTATOR == null ? "None" : config.MUTATOR.name();
            return config.getClass().getSimpleName() + " — " + config.GAME_LENGTH.text
                    + ", " + config.TEAM_SIZE_MAX.name + ", " + config.XP_RATE + "x XP, Mutator: " + mutator;
        } catch (Throwable t) {
            return config.getClass().getSimpleName();
        }
    }

    private static String rankOf(Player p) {
        try {
            return p.getPrimaryGroup() == null ? "REGISTERED" : p.getPrimaryGroup().name();
        } catch (Throwable t) {
            return "?";
        }
    }

    private static String positionOf(Player p) {
        try {
            return p.getAbsX() + ", " + p.getAbsY() + ", " + p.getHeight();
        } catch (Throwable t) {
            return "?";
        }
    }

    // --- tiny defensive helpers so one null/race never aborts a whole snapshot ---

    private interface ThrowingInt { int get() throws Throwable; }
    private interface ThrowingStr { String get() throws Throwable; }
    private interface ThrowingBool { boolean get() throws Throwable; }
    private interface ThrowingCfg { TournamentConfig get() throws Throwable; }

    private static int safeCount(ThrowingInt s) {
        try { return s.get(); } catch (Throwable t) { return 0; }
    }

    private static String safeString(ThrowingStr s, String fallback) {
        try { String v = s.get(); return v == null ? fallback : v; } catch (Throwable t) { return fallback; }
    }

    private static boolean safeBool(ThrowingBool s) {
        try { return s.get(); } catch (Throwable t) { return false; }
    }

    private static TournamentConfig safe(ThrowingCfg s) {
        try { return s.get(); } catch (Throwable t) { return null; }
    }
}
