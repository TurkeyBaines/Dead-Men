package io.dm.network.incoming.handlers.commands;

import io.dm.deadman.sigils.Sigil;
import io.dm.deadman.sigils.Sigils;
import io.dm.model.entity.Entity;
import io.dm.model.entity.player.Player;

public class SigilCommands {

    public void process(Player player, String... args) {
        switch (args[0]) {

            case "reset": case "r":
                reset(player, args);
                break;

            case "unlock": case "u":
                unlock(player, args);
                break;

            case "activate": case "a":
                activate(player, args);
                break;

            case "deactivate": case "d":
                deactivate(player, args);
                break;

            case "print": case "p":
                print(player, args);
                break;
        }
    }


    private void reset(Player player, String... args) {
        switch (args[1]) {
            case "all":
                Sigil.reset(player);
                player.sendMessage("Reset ALL Sigils");
                return;

            case "active":
                for (int i = 0; i < 3; i++)
                    player.activeSigils[i] = -1;
                player.sendMessage("Reset ACTIVE Sigils");
                return;

            case "unlocked":
                for (int i = 0; i < 40; i++)
                    player.unlockedSigils[i] = false;
                player.sendMessage("Reset UNLOCKED Sigils");
                return;

            case "toggle":
                for (int i = 0; i < 4; i++)
                    player.toggleSigils[i] = new boolean[]{false, false};
                player.sendMessage("Reset TOGGLE Sigils");
                return;
        }
        return;
    }

    private void unlock(Player player, String... args) {
        if (args == null) {
            player.sendMessage("Please enter an argument!");
            return;
        }
        switch (args[1]) {
            case "all":
                for (int i = 0; i < Sigils.values().length; i++) {
                    Sigil.unlock(player, Sigils.values()[i]);
                }
                player.sendMessage("Unlocked ALL Sigils!");
                return;

            case "test":
                Sigil.unlock(player, Sigils.Potion_Master);
                Sigil.unlock(player, Sigils.Faith);
                Sigil.unlock(player, Sigils.Food_Master);
                player.sendMessage("Unlocked TEST Sigils!");
                return;

            case "toggle":
                if (Sigil.fromToggleID(Integer.parseInt(args[2])) != null) {
                    Sigil.unlock(player, Sigil.fromToggleID(Integer.parseInt(args[2])));
                    return;
                } else {
                    player.sendMessage("This ID is null! use '::print_sigil all' to view the IDs");
                }

            default:
                if (Sigil.fromID(Integer.parseInt(args[1])) != null) {
                    Sigil.unlock(player, Sigil.fromID(Integer.parseInt(args[1])));
                    return;
                } else {
                    player.sendMessage("This ID is null! use '::print_sigil all' to view the IDs");
                }
        }
        return;
    }

    private void activate(Player player, String... args) {
        String namex = args[1];
        Sigils sigil = null;
        System.out.println(namex);

        for (Sigils s : Sigils.values()) if (namex.equalsIgnoreCase(s.name())) sigil = s;
        if (sigil == null) { player.sendMessage("Sigil Name is not correct."); return; }
        else if (!Sigil.playerUnlocked(player, sigil)) { player.sendMessage("You do not have this Sigil unlocked. ID: " + sigil.id); return; }
        else if (!Sigil.canActivate(player)) { player.sendMessage("You do not have enough free slots to activate."); return; }

        Sigil.activate(player, sigil);
        player.sendMessage("Activated " + sigil.name().replace("_", " ") + " Sigil!");
        return;
    }

    private void deactivate(Player player, String... args) {
        int index = Integer.parseInt(args[1]);
        if (index >= 3) return;;
        player.activeSigils[index] = -1;
        player.sendMessage("Deactivated ACTIVE[" + index + "] Sigil");
        return;
    }

    private void print(Player player, String... args) {
        String name;
        switch (args[1]) {
            case "all":
                player.sendMessage("== UNLOCKED SIGILS ==");
                for (int i = 0; i < 40; i++) {
                    name = Sigil.fromID(i) == null ? "null" : Sigil.fromID(i).name().replace("_", " ");
                    player.sendMessage("[" + i + "] " + name + ": " + (player.unlockedSigils[i] == false ? "locked" : "unlocked"));
                }
                player.sendMessage("");
                player.sendMessage("== TOGGLE SIGILS ==");
                for (int i = 0; i < 4; i++) {
                    name = Sigil.fromToggleID(i) == null ? "null" : Sigil.fromID(i).name().replace("_", " ");
                    player.sendMessage("[" + i + "] " + name + ": " + (player.toggleSigils[i][0] == false ? "locked" : player.toggleSigils[i][1] == true ? "activated" : "deactivated"));
                }
                player.sendMessage("");
                player.sendMessage("== ACTIVE SIGILS ==");
                for (int i = 0; i < 3; i++) {
                    if (player.activeSigils[i] == -1) {
                        player.sendMessage("[" + i + "] EMPTY");
                    } else {
                        player.sendMessage("[" + i + "] " + Sigil.fromID(player.activeSigils[i]).name() + ": ACTIVE");
                    }
                }
                return;

            case "unlocked":
                player.sendMessage("== UNLOCKED SIGILS ==");
                for (int i = 0; i < 40; i++) {
                    name = Sigil.fromID(i) == null ? "null" : Sigil.fromID(i).name().replace("_", " ");
                    player.sendMessage("[" + i + "] " + name + ": " + (player.unlockedSigils[i] == false ? "locked" : "unlocked"));
                }
                return;

            case "toggle":
                player.sendMessage("== TOGGLE SIGILS ==");
                for (int i = 0; i < 4; i++) {
                    name = Sigil.fromToggleID(i) == null ? "null" : Sigil.fromID(i).name().replace("_", " ");
                    player.sendMessage("[" + i + "] " + name + ": " + (player.toggleSigils[i][0] == false ? "locked" : player.toggleSigils[i][1] ? "activated" : "deactivated"));
                }
                return;

            case "active":
                player.sendMessage("== ACTIVE SIGILS ==");
                for (int i = 0; i < 3; i++) {
                    if (player.activeSigils[i] == -1) {
                        player.sendMessage("[" + i + "] EMPTY");
                    } else {
                        player.sendMessage("[" + i + "] " + Sigil.fromID(player.activeSigils[i]).name() + ": ACTIVE");
                    }
                }
                return;

            case "curse":
                player.sendMessage("Your curse data: NEXT_TICK[" + player.getCombat().nextCurseTicks + "] - CAN_CURSE: " + player.getCombat().canCurse());

                if (player.getCombat().getTarget() == null) {
                    player.sendMessage("No Target to display.");
                    return;
                }
                Entity target = player.getCombat().getTarget();
                if (target.player != null)
                    player.sendMessage(target.player.getName() + "'s curse data: NEXT_TICK[" + target.player.getCombat().nextCurseTicks + "] - CAN_CURSE: " + target.player.getCombat().canCurse());
                else
                    player.sendMessage(target.player.getName() + "'s curse data: NEXT_TICK[" + target.npc.getCombat().nextCurseTicks + "] - CAN_CURSE: " + target.npc.getCombat().canCurse());
                return;
        }
    }
}
