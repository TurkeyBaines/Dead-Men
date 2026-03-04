package io.dm.network.incoming.handlers.commands;

import io.dm.model.World;
import io.dm.model.entity.player.Player;
import io.dm.model.stat.Stat;
import io.dm.model.stat.StatType;

public class StatCommands {

    public void process(Player player, String... args) {
        switch (args[0]) {
            case "set": case "setlvl": case "setlvls":
            case "setlevel": case "setlevels":
                set(player, args);
                break;

            case "master": case "max":
                master(player);
                break;

            case "xmaster": case "maxother": case "masterother":
                master(player, args);
                break;

            case "reset": case "resetme":
                reset(player);
                break;

            case "xreset": case "resetother":
                reset(player, args);
                break;
        }
    }

    private void set(Player player, String... args) {
        int id = Integer.parseInt(args[1]);
        int lvl = Integer.parseInt(args[2]);
        player.getStats().get(StatType.values()[id]).currentLevel = lvl;
        player.getStats().get(StatType.values()[id]).experience = Stat.xpForLevel(lvl);
        player.getStats().get(StatType.values()[id]).updated = true;
    }

    private void master(Player player) {
        for (StatType type : StatType.values()) {
            player.getStats().set(type, 99, 200000000);
        }
    }

    private void master(Player player, String... args) {
        Player p2 = World.getPlayer(args[1].replace("_", " "));
        if (p2 == null) return;
        for (StatType type : StatType.values()) {
            p2.getStats().set(type, 99, 200000000);
        }
    }

    private void reset(Player player) {
        player.getStats().reset();
    }

    private void reset(Player player, String... args) {
        Player p2 = World.getPlayer(args[1].replace("_", " "));
        if (p2 == null) return;
        p2.getStats().reset();
    }

}
