package io.dm.panel.core;

import io.dm.Server;
import io.dm.deadman.Deadman;
import io.dm.deadman.tournament.Tournament;
import io.dm.deadman.tournament.events.DMMEvent;
import io.dm.deadman.tournament.stages.Final;
import io.dm.deadman.tournament.stages.Lobby;
import io.dm.deadman.tournament.stages.Main;
import io.dm.model.World;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.player.PlayerFile;
import io.dm.model.shop.Currency;
import io.dm.network.incoming.handlers.CommandHandler;
import io.dm.services.Punishment;
import io.dm.api.utils.IPMute;

/**
 * The single chokepoint for every mutating operator action taken from the Server Panel.
 *
 * <p>Every method here posts its work onto the game thread via
 * {@code Server.worker.execute(Runnable)} — the exact pattern {@code Punishment} and
 * {@code TournamentCommands} already use — so the JavaFX thread never mutates game state
 * directly. All business logic is delegated to existing server code
 * ({@code Punishment.*}, {@code Deadman.*}, {@code CommandHandler.handle}, {@code Currency},
 * {@code World.*}); nothing is reimplemented here.
 *
 * <p>Player targets are passed by <em>name</em> and resolved fresh inside the game-thread
 * task, so a target logging out between the click and the tick can never act on a stale
 * reference. Each action also echoes a line into {@link LogFeed} so operator actions are
 * visible in the same console feed as everything else.
 */
public final class ActionDispatcher {

    private ActionDispatcher() {}

    private static void onGameThread(Runnable r) {
        Server.worker.execute(r);
    }

    private static void audit(String message) {
        LogFeed.INSTANCE.append("PANEL", "panel", message);
    }

    // ------------------------------------------------------------------ moderation

    public static void kick(String targetName) {
        onGameThread(() -> {
            Player staff = PanelState.resolveActingStaff();
            Player target = World.getPlayer(targetName);
            if (target == null) { audit("Kick failed: '" + targetName + "' is not online."); return; }
            if (staff == null)  { audit("Kick failed: no online staff to act as."); return; }
            Punishment.kick(staff, target);
            audit(staff.getName() + " kicked " + target.getName() + ".");
        });
    }

    public static void ban(String targetName) {
        onGameThread(() -> {
            Player staff = PanelState.resolveActingStaff();
            Player target = World.getPlayer(targetName);
            if (target == null) { audit("Ban failed: '" + targetName + "' is not online."); return; }
            if (staff == null)  { audit("Ban failed: no online staff to act as."); return; }
            Punishment.ban(staff, target);
            audit(staff.getName() + " banned " + target.getName() + ".");
        });
    }

    public static void mute(String targetName, long durationMs, boolean shadow) {
        onGameThread(() -> {
            Player staff = PanelState.resolveActingStaff();
            Player target = World.getPlayer(targetName);
            if (target == null) { audit("Mute failed: '" + targetName + "' is not online."); return; }
            if (staff == null)  { audit("Mute failed: no online staff to act as."); return; }
            long end = durationMs < 0 ? -1L : System.currentTimeMillis() + durationMs;
            Punishment.mute(staff, target, end, shadow);
            audit(staff.getName() + (shadow ? " shadow-muted " : " muted ") + target.getName() + ".");
        });
    }

    // ------------------------------------------------------------------ tournament

    public static void forceStage(Tournament.StageName stage) {
        onGameThread(() -> {
            try {
                Deadman.getStage().onRemove();
            } catch (Throwable ignored) {
                // Some stages tolerate onRemove being called defensively; ignore.
            }
            Deadman.setStage(switch (stage) {
                case LOBBY -> new Lobby();
                case MAIN -> new Main();
                case FINAL -> new Final();
            });
            Deadman.getStage().onLoad();
            audit("Tournament stage forced to " + stage.name() + ".");
        });
    }

    /** Spawn a live event, mirroring TournamentCommands.spawnLiveEvent (MAIN only). */
    public static void spawnLiveEvent(DMMEvent event) {
        onGameThread(() -> {
            if (!(Deadman.getStage() instanceof Main main)) {
                audit("Cannot spawn event: not in MAIN stage.");
                return;
            }
            if (main.currentEvent != null) {
                audit("Cannot spawn event: one is already active.");
                return;
            }
            main.currentEvent = event;
            main.nextEvent += 120000;
            event.spawn();
            audit("Spawned live event: " + event.getClass().getSimpleName() + ".");
        });
    }

    /** Despawn the current live event, mirroring TournamentCommands "event kill". */
    public static void killCurrentEvent() {
        onGameThread(() -> {
            if (!(Deadman.getStage() instanceof Main main)) {
                audit("Cannot kill event: not in MAIN stage.");
                return;
            }
            if (main.currentEvent == null) {
                audit("Cannot kill event: none active.");
                return;
            }
            String name = main.currentEvent.getClass().getSimpleName();
            main.currentEvent.despawn();
            main.currentEvent = null;
            main.nextEvent = System.currentTimeMillis() + 900000;
            audit("Killed live event: " + name + ".");
        });
    }

    // ------------------------------------------------------------------ economy

    public static void grantOverworldPoints(String targetName, int delta) {
        onGameThread(() -> {
            Player target = World.getPlayer(targetName);
            if (target == null) { audit("Grant failed: '" + targetName + "' is not online."); return; }
            Currency.COINS.getCurrencyHandler().addCurrency(target, delta);
            audit("Granted " + delta + " Overworld Points to " + target.getName() + ".");
        });
    }

    // ------------------------------------------------------------------ player editing

    /** Set a single skill's level (1-99), mirroring the proven ::stat set logic. */
    public static void setStat(String targetName, int statOrdinal, int level) {
        onGameThread(() -> withPlayer(targetName, p -> {
            applyStat(p, io.dm.model.stat.StatType.values()[statOrdinal], level);
            audit("Set " + p.getName() + "'s " + io.dm.model.stat.StatType.values()[statOrdinal].name() + " to " + level + ".");
        }));
    }

    public static void maxStats(String targetName) {
        onGameThread(() -> withPlayer(targetName, p -> {
            for (io.dm.model.stat.StatType t : io.dm.model.stat.StatType.values()) applyStat(p, t, 99);
            audit("Maxed all stats for " + p.getName() + ".");
        }));
    }

    public static void resetStats(String targetName) {
        onGameThread(() -> withPlayer(targetName, p -> { p.getStats().reset(); audit("Reset all stats for " + p.getName() + "."); }));
    }

    /** Un-boost / un-drain: restore each skill's current level to its base level. */
    public static void restoreStats(String targetName) {
        onGameThread(() -> withPlayer(targetName, p -> {
            for (io.dm.model.stat.StatType t : io.dm.model.stat.StatType.values()) {
                io.dm.model.stat.Stat st = p.getStats().get(t);
                st.currentLevel = st.fixedLevel;
                st.updated = true;
            }
            audit("Restored levels for " + p.getName() + ".");
        }));
    }

    public static void giveItem(String targetName, int itemId, int amount) {
        onGameThread(() -> withPlayer(targetName, p -> {
            p.getInventory().add(itemId, amount);
            audit("Gave " + amount + " x " + ItemIconService.nameOf(itemId) + " to " + p.getName() + ".");
        }));
    }

    public static void addToBank(String targetName, int itemId, int amount) {
        onGameThread(() -> withPlayer(targetName, p -> {
            p.getBank().add(itemId, amount);
            audit("Banked " + amount + " x " + ItemIconService.nameOf(itemId) + " for " + p.getName() + ".");
        }));
    }

    public static void removeItem(String targetName, int itemId, int amount, boolean fromBank) {
        onGameThread(() -> withPlayer(targetName, p -> {
            if (fromBank) {
                p.getBank().remove(itemId, amount);
            } else {
                p.getInventory().remove(itemId, amount);
            }
            audit("Removed " + amount + " x " + ItemIconService.nameOf(itemId)
                    + " from " + p.getName() + "'s " + (fromBank ? "bank." : "inventory."));
        }));
    }

    /** Read a player's inventory and bank contents on the game thread. */
    public static void readContainers(String targetName,
                                      java.util.function.BiConsumer<java.util.List<ItemStack>, java.util.List<ItemStack>> callback) {
        onGameThread(() -> {
            java.util.List<ItemStack> inv = new java.util.ArrayList<>();
            java.util.List<ItemStack> bank = new java.util.ArrayList<>();
            Player p = World.getPlayer(targetName);
            if (p != null) {
                collectItems(safeItems(() -> p.getInventory().getItems()), inv);
                collectItems(safeItems(() -> p.getBank().getItems()), bank);
            }
            javafx.application.Platform.runLater(() -> callback.accept(inv, bank));
        });
    }

    private static void collectItems(io.dm.model.item.Item[] items, java.util.List<ItemStack> out) {
        if (items == null) return;
        for (io.dm.model.item.Item item : items) {
            if (item != null && item.getId() > 0) {
                out.add(new ItemStack(item.getId(), item.getAmount()));
            }
        }
    }

    public static void teleport(String targetName, int x, int y, int z) {
        onGameThread(() -> withPlayer(targetName, p -> {
            p.getMovement().teleport(x, y, z);
            audit("Teleported " + p.getName() + " to " + x + ", " + y + ", " + z + ".");
        }));
    }

    public static void setOverworldPoints(String targetName, int value) {
        onGameThread(() -> withPlayer(targetName, p -> { p.appreciationPoints = value; audit("Set " + p.getName() + " Overworld Points = " + value); }));
    }

    public static void setVaultPoints(String targetName, int value) {
        onGameThread(() -> withPlayer(targetName, p -> { p.appreciationPoints = value; audit("Set " + p.getName() + " Vault Points = " + value); }));
    }

    public static void setPvmPoints(String targetName, int value) {
        onGameThread(() -> withPlayer(targetName, p -> { p.PvmPoints = value; audit("Set " + p.getName() + " PvM Points = " + value); }));
    }

    public static void setAppreciationPoints(String targetName, int value) {
        onGameThread(() -> withPlayer(targetName, p -> { p.appreciationPoints = value; audit("Set " + p.getName() + " Appreciation Points = " + value); }));
    }

    /** Read current skill levels (indexed by StatType ordinal) for pre-filling the editor. */
    public static void readPlayerLevels(String targetName, java.util.function.Consumer<int[]> callback) {
        onGameThread(() -> {
            Player p = World.getPlayer(targetName);
            int[] levels = null;
            if (p != null) {
                try {
                    io.dm.model.stat.StatType[] types = io.dm.model.stat.StatType.values();
                    levels = new int[types.length];
                    for (int i = 0; i < types.length; i++) levels[i] = p.getStats().get(types[i]).currentLevel;
                } catch (Throwable ignored) { levels = null; }
            }
            int[] result = levels;
            javafx.application.Platform.runLater(() -> callback.accept(result));
        });
    }

    private static void applyStat(Player p, io.dm.model.stat.StatType type, int level) {
        io.dm.model.stat.Stat st = p.getStats().get(type);
        st.currentLevel = level;
        st.experience = io.dm.model.stat.Stat.xpForLevel(level);
        st.updated = true;
    }

    private static void withPlayer(String name, java.util.function.Consumer<Player> action) {
        Player p = World.getPlayer(name);
        if (p == null) { audit("Action failed: '" + name + "' is not online."); return; }
        action.accept(p);
    }

    // ------------------------------------------------------------------ world / misc

    public static void broadcast(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        onGameThread(() -> {
            World.players.forEach(p -> p.getPacketSender().sendMessage(message, "", 14));
            audit("Broadcast: " + message);
        });
    }

    public static void toggleDoubleDrops() { onGameThread(() -> { World.toggleDoubleDrops(); audit("Toggled double drops."); }); }
    public static void toggleDoublePkp()   { onGameThread(() -> { World.toggleDoublePkp();   audit("Toggled double PKP."); }); }
    public static void toggleDoubleSlayer(){ onGameThread(() -> { World.toggleDoubleSlayer();audit("Toggled double slayer."); }); }
    public static void toggleDoublePest()  { onGameThread(() -> { World.toggleDoublePest();  audit("Toggled double pest."); }); }
    public static void toggleWeekendExp()  { onGameThread(() -> { World.toggleWeekendExpBoost(); audit("Toggled weekend XP boost."); }); }
    public static void toggleDmmKeyEvent() { onGameThread(() -> { World.toggleDmmKeyEvent(); audit("Toggled DMM key event."); }); }
    public static void toggleWildyKeyEvent(){ onGameThread(() -> { World.toggleWildernessKeyEvent(); audit("Toggled wilderness key event."); }); }

    public static void setBloodMoneyMultiplier(int multiplier) {
        onGameThread(() -> { World.boostBM(multiplier); audit("Set blood money multiplier to x" + multiplier + "."); });
    }

    public static void setBaseBloodMoney(int base) {
        onGameThread(() -> { World.setBaseBloodMoney(base); audit("Set base blood money per kill to " + base + "."); });
    }

    public static void supplyChestBroadcast(String message) {
        if (message == null || message.isBlank()) return;
        onGameThread(() -> { World.sendSupplyChestBroadcast(message); audit("Supply chest broadcast: " + message); });
    }

    // ------------------------------------------------------------------ season / config

    /** Rotate the tournament code — players are reset to a fresh account on next login. */
    public static void newTournamentCode() {
        onGameThread(() -> { Deadman.generateUTC(); audit("Generated a NEW tournament code — players reset on next login."); });
    }

    /**
     * Read a snapshot of a player's data (stats, points, inventory, equipment, bank)
     * on the game thread, then deliver the formatted text back on the JavaFX thread.
     */
    public static void inspectPlayer(String targetName, java.util.function.Consumer<String> callback) {
        onGameThread(() -> {
            Player p = World.getPlayer(targetName);
            String text = p == null ? "'" + targetName + "' is no longer online." : describePlayer(p);
            javafx.application.Platform.runLater(() -> callback.accept(text));
        });
    }

    private static String describePlayer(Player p) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(p.getName()).append("  [").append(p.getPrimaryGroup()).append("]\n");
            sb.append("Position: ").append(p.getAbsX()).append(", ").append(p.getAbsY()).append(", ").append(p.getHeight()).append('\n');
            sb.append("IP: ").append(p.getIp()).append("   UUID: ").append(p.getUUID()).append('\n');
            sb.append("Overworld: ").append(p.appreciationPoints).append("  Vault: ").append(p.appreciationPoints)
              .append("  PvM: ").append(p.PvmPoints).append("  Appreciation: ").append(p.appreciationPoints).append("\n\n");
        } catch (Throwable ignored) {}
        try {
            sb.append("── STATS ──\n");
            for (io.dm.model.stat.StatType st : io.dm.model.stat.StatType.values()) {
                io.dm.model.stat.Stat stat = p.getStats().get(st.ordinal());
                sb.append(String.format("%-14s %3d (xp %,d)%n", st.name(), stat.currentLevel, (long) stat.experience));
            }
        } catch (Throwable ignored) {}
        appendContainer(sb, "\n── EQUIPMENT ──", safeItems(() -> p.getEquipment().getItems()));
        appendContainer(sb, "\n── INVENTORY ──", safeItems(() -> p.getInventory().getItems()));
        try {
            sb.append("\n── BANK ──\n").append(p.getBank().getCount()).append(" item(s) stored.");
        } catch (Throwable ignored) {}
        return sb.toString();
    }

    private interface ItemsSupplier { io.dm.model.item.Item[] get() throws Throwable; }

    private static io.dm.model.item.Item[] safeItems(ItemsSupplier s) {
        try { return s.get(); } catch (Throwable t) { return null; }
    }

    private static void appendContainer(StringBuilder sb, String header, io.dm.model.item.Item[] items) {
        sb.append(header).append('\n');
        if (items == null) { sb.append("(unavailable)\n"); return; }
        int shown = 0;
        for (io.dm.model.item.Item item : items) {
            if (item == null) continue;
            String name;
            try { name = io.dm.cache.ItemDef.get(item.getId()).name; } catch (Throwable t) { name = "item " + item.getId(); }
            sb.append(String.format("  %s x%,d%n", name, item.getAmount()));
            shown++;
        }
        if (shown == 0) sb.append("(empty)\n");
    }

    /** Pre-select the config the next tournament will use. */
    public static void overrideNextConfig(io.dm.deadman.tournament.TournamentConfig config, String label) {
        onGameThread(() -> {
            Deadman.overrideConfig(config);
            Deadman.setNext_Config_Name(label);
            audit("Next tournament config overridden: " + label + ".");
        });
    }

    // ------------------------------------------------------------------ admin utilities

    public static void saveAllPlayers() {
        onGameThread(() -> {
            int n = 0;
            for (Player p : World.players) { if (p != null) { PlayerFile.save(p, -1); n++; } }
            audit("Saved " + n + " online player(s).");
        });
    }

    public static void removeAllPlayers() {
        onGameThread(() -> { World.players.forEach(Player::forceLogout); audit("Force-logged out all online players."); });
    }

    public static void reloadMutes() {
        onGameThread(() -> { IPMute.refreshMutes(); audit("Reloaded IP mutes."); });
    }

    public static void systemUpdate(int minutes) {
        onGameThread(() -> {
            World.update(minutes);
            audit("System update triggered: " + minutes + " minute(s).");
        });
    }

    /**
     * Run an arbitrary {@code ::} command string through the real command handler,
     * acting as the given online staff player. This gives the panel full coverage of
     * every existing debug command with no per-command wiring.
     */
    public static void runCommand(String actingStaffName, String query) {
        if (query == null || query.isBlank()) {
            return;
        }
        String cleaned = query.startsWith("::") ? query.substring(2) : query;
        onGameThread(() -> {
            Player staff = actingStaffName != null ? World.getPlayer(actingStaffName) : PanelState.resolveActingStaff();
            if (staff == null) {
                audit("Command failed: no acting staff player online.");
                return;
            }
            audit(staff.getName() + " ran: ::" + cleaned);
            CommandHandler.handle(staff, cleaned);
        });
    }

    /** Graceful full shutdown — runs the existing Runtime shutdown hook in Server.java. */
    public static void shutdownServer() {
        audit("Operator requested server shutdown.");
        System.exit(0);
    }
}
