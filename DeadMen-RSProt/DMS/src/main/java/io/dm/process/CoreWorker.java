package io.dm.process;

import io.dm.Server;
import io.dm.api.utils.Random;
import io.dm.event.GameEventProcessor;
import io.dm.model.World;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.Player;
import io.dm.model.object.owned.OwnedObject;

public class CoreWorker extends World {

    private static Stage processStage = Stage.INDEX;
    public static long LAST_CYCLE_UPDATE;

    public enum Stage {

        INDEX(CoreWorker::index),
        LOGIC(CoreWorker::logic),
        UPDATE(CoreWorker::update);

        private final Runnable runnable;

        Stage(Runnable runnable) {
            this.runnable = runnable;
        }

    }

    public static void process() {
        for(Stage stage : Stage.values()) {
            processStage = stage;
            processStage.runnable.run();
        }
        LAST_CYCLE_UPDATE = System.currentTimeMillis();
    }

    public static boolean isPast(Stage stage) {
        return processStage.ordinal() > stage.ordinal();
    }

    /**
     * Index - Used for indexing players & npcs.
     */

    private static int scrambleTicks;

    private static void index() {
        /**
         * Player indexing
         */
        players.resetCount();
        for(Player player : players.entityList) {
            if(player != null)
                players.index(player);
        }
        /**
         * Npc indexing
         */
        npcs.resetCount();
        for(NPC npc : npcs.entityList) {
            if(npc != null)
                npcs.index(npc);
        }
        /*
         * Scrambling
         */
        if(--scrambleTicks <= 0) {
            scrambleTicks = Random.get(40, 60);
            players.scramble();
            npcs.scramble();
        }
    }

    /**
     * Logic - Things like packet handling, combat, movement, etc.
     */
    private static void logic() {
        GameEventProcessor.pulse();
        for(Player player : players) {
            try {
                player.checkLogout();
            } catch(Throwable t) {
                Server.logError("", t);
            }
        }
        // Stage-3 bind: drain each RSProt player's decoded incoming packets (e.g. movement clicks →
        // routeAbsolute) BEFORE process(), so the movement queue is populated for this cycle's walk.
        for(Player player : players) {
            try {
                if(player.isOnline())
                    io.dm.network.rsprot.RsProt.processIncoming(player);
            } catch(Throwable t) {
                Server.logError("", t);
            }
        }
        for(Player player : players) {
            try {
                if(player.isOnline()) {
                    player.processed = true;
                    player.process();
                }
            } catch(Throwable t) {
                Server.logError("", t);
            }
        }
        for(NPC npc : npcs) {
            try {
                npc.processed = true;
                npc.process();
            } catch(Throwable t) {
                Server.logError("", t);
            }
        }
        for (OwnedObject object : ownedObjects.values()) {
            try {
                object.tick();
            } catch (Throwable t) {
                Server.logError("", t);
            }
        }
    }

    /**
     * Update - Things like updating local players, local npcs, writing packets to the channel, etc.
     */

    private static void update() {
        // Stage-3 bind: drive the RSProt player/npc/world-entity info protocols for all RSProt
        // players (one call, handles the whole world in Alter's sequence + flushes each session).
        io.dm.network.rsprot.RsProt.driveUpdate();
        for(Player player : players) {
            try {
                if(player.isOnline()) {
                    // Legacy 184 GPI/NPI only for players still on the old transport (none once fully
                    // bound). RSProt players (session != null) get their player/npc info from
                    // driveUpdate() above.
                    if(player.getSession() == null) {
                        player.getUpdater().process();
                        player.getNpcUpdater().process();
                    }
                    // Varps route through PacketSender (ported to RSProt Varp{Small,Large} for RSProt
                    // players) — sends only changed varps. Needed for skills/settings + the display-name
                    // varbit that unlocks public chat.
                    player.sendVarps();
                }
            } catch(Throwable t) {
                Server.logError("", t);
            }
        }
        for(Player player : players) {
            try {
                if(player.isOnline())
                    player.resetUpdates();
                if(player.getChannel() != null) // RSProt players have no legacy Netty channel
                    player.getChannel().flush();
                player.processed = false;
            } catch(Throwable t) {
                Server.logError("", t);
            }
        }
        for(NPC npc : npcs) {
            try {
                npc.resetUpdates();
                npc.processed = false;
            } catch(Throwable t) {
                Server.logError("", t);
            }
        }
    }

}