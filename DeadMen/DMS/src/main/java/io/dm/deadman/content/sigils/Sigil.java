package io.dm.deadman.content.sigils;

import io.dm.Server;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.utils.Option;
import io.dm.model.item.Item;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Sigil {

    public abstract int ID();
    public abstract int Chance();

    public abstract int Cooldown();

    public abstract String name();

    public boolean trigger() {
        return isActionSuccessful();
    }

    public boolean trigger(Player p, Sigils s) {
        return isActionSuccessful() && !hasCooldown(p, s);
    }

    public boolean hasCooldown(Player p, Sigils s) {
        int slot = getSlot(p, s);
        long cooldown = p.activeCooldowns[slot];

        return Server.currentTick() < cooldown;
    }

    public void addCooldown(Player p, Sigils s) {
        int slot = getSlot(p, s);
        int addition = Cooldown();
        p.activeCooldowns[slot] = (Server.currentTick() + addition);
    }

    public Sigil() {}

    public boolean effect() {return false;}
    public boolean effect(Player player) {return false;}
    public boolean effect(Player player, int params) {return false;}
    public boolean effect(Player player, int[] params) {return false;}
    public boolean effect(Player player, String params) {return false;}
    public boolean effect(Player player, String[] params) {return false;}
    public boolean effect(Player player, Player target){return false;}
    public boolean effect(Player player, Player target, int params){return false;}
    public boolean effect(Player player, Player target, String params){return false;}
    public boolean effect(Player player, Player target, int[] params){return false;}
    public boolean effect(Player player, Player target, String[] params){return false;}
    public boolean effect(Player player, NPC target){return false;}
    public boolean effect(Player player, NPC target, int params){return false;}
    public boolean effect(Player player, NPC target, String params){return false;}
    public boolean effect(Player player, NPC target, int[] params){return false;}
    public boolean effect(Player player, NPC target, String[] params){return false;}
    public boolean effect(Player player, Item item){return false;}
    public boolean effect(Player player, Item item, int params){return false;}
    public boolean effect(Player player, Item item, String params){return false;}
    public boolean effect(Player player, Item item, int[] params){return false;}
    public boolean effect(Player player, Item item, String[] params){return false;}

    public boolean RestrictedWith() { return false; }

    public boolean isActionSuccessful() {
        int successChance = Chance();
        // Generate a random number between 1 and 100 (inclusive)
        int roll = ThreadLocalRandom.current().nextInt(1, 101);

        return roll <= successChance;
    }

    public static boolean sigilActive(Player player, Sigils sigil) {
        for (int i : player.activeSigils)
            if (i == sigil.id)
                return true;
        return false;
    }

    public static boolean playerUnlocked(Player player, Sigils sigil) {
        return player.unlockedSigils[sigil.id];
    }

    public static boolean playerUnlocked(Player player, ToggleSigils sigil) {
        return player.toggleSigils[sigil.id][0];
    }

    public static Sigil get(Sigils sigil) {
        return activeSigilMap.get(sigil);
    }

    public static Sigil get(ToggleSigils sigil) {
        return toggleSigilMap.get(sigil);
    }

    public static void unlock(Player player, Sigils sigil) {
        if (playerUnlocked(player, sigil)) return;

        player.unlockedSigils[sigil.id] = true;
        player.sendMessage("You unlock the Sigil of " + (sigil.name().replace("_", " ")));
    }

    public static void unlock(Player player, ToggleSigils sigil) {
        if (playerUnlocked(player, sigil)) return;

        player.dialogue(
                new OptionsDialogue("Would you like to activate your Passive Sigil of " + sigil.name() + " now?",
                        new Option("Yes", () -> {
                            player.toggleSigils[sigil.id] = new boolean[]{true, true};
                        }),
                        new Option("No", () -> {
                            player.toggleSigils[sigil.id] = new boolean[]{true, true};
                        })
                )
        );

        player.sendMessage("You unlock the Toggleable Sigil of " + (sigil.name().replace("_", " ")));
    }

    public static boolean canActivate(Player player) {
        return player.activeSigils[0] == -1 || player.activeSigils[1] == -1 || player.activeSigils[2] == -1;
    }

    public static void activate(Player player, Sigils sigil) {
        if (!playerUnlocked(player, sigil)) return;
        if (sigilActive(player, sigil)) return;
        if (!canActivate(player)) return;

        for (int i = 0; i < 3; i++) {
            if (player.activeSigils[i] == -1) {
                player.activeSigils[i] = sigil.id;
                return;
            }
        }
    }

    public static void deactivate(Player player, Sigils sigil) {
        if (!playerUnlocked(player, sigil)) return;
        if (!sigilActive(player, sigil)) return;

        for (int i = 0; i < 3; i++) {
            if (player.activeSigils[i] == sigil.id)
                player.activeSigils[i] = -1;
        }
    }

    public static void reset(Player player) {
        for (int i = 0; i < 40; i++) player.unlockedSigils[i] = false;

        for (int i = 0; i < 3; i++) player.activeSigils[i] = -1;

        for (int i = 0; i < 4; i++) player.toggleSigils[i] = new boolean[]{false, false};
    }

    public static Sigils fromID(int id) {
        for (Sigils s : Sigils.values()) {
            if (s.id == id)
                return s;
        }
        return null;
    }

    public static Sigils fromToggleID(int id) {
        for (Sigils s : Sigils.values()) {
            if (s.id == id)
                return s;
        }
        return null;
    }

    private static int getSlot(Player player, Sigils sigil) {
        int id = sigil.id;

        for (int i = 0; i < 3; i++) {
            if (player.activeSigils[i] == id)
                return i;
        }
        return -1;
    }

    private static HashMap<Sigils, Sigil> activeSigilMap;
    private static HashMap<ToggleSigils, Sigil> toggleSigilMap;

    static {
        activeSigilMap = new HashMap<>();
        toggleSigilMap = new HashMap<>();

        for (Sigils s : Sigils.values()) {
            activeSigilMap.put(s, s.create());
        }

        for (ToggleSigils ts : ToggleSigils.values()) {
            toggleSigilMap.put(ts, ts.create());
        }
    }


}
