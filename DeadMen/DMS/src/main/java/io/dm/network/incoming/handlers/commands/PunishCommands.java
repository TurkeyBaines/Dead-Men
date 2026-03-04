package io.dm.network.incoming.handlers.commands;

import io.dm.api.utils.IPBans;
import io.dm.api.utils.IPMute;
import io.dm.model.World;
import io.dm.model.entity.player.Player;
import io.dm.services.Punishment;

public class PunishCommands {

    public void process(Player p, String... args) {
        Player p2 = World.getPlayer(args[1].replace("_", " "));
        if (p2 == null) {
            p.sendMessage("That p doesn't exist, or is offline!");
            return;
        }
        switch (args[0]) {

            case "ban":
                ban(p, p2, false);
                break;

            case "ipban":
                ban(p, p2, true);
                break;

            case "mute":
                mute(p, p2, false, args);
                break;

            case "ipmute":
                mute(p, p2, true);
                break;

            case "macban":
                mac(p, p2);
                break;

            case "reload":
                reload();
                break;

        }
    }

    private void ban(Player p, Player p2, boolean ip) {
        if (ip)
            Punishment.ipBan(p, p2);
        else
            Punishment.ban(p, p2);
    }

    private void mute(Player p, Player p2, boolean ip, String... args) {
        if (ip) {
            Punishment.ipMute(p, p2);
            return;
        }

        long time = 3600000;
        boolean shadow = false;
        if (args[2] != null) time = Long.parseLong(args[2]);
        if (args[3] != null) shadow = Boolean.parseBoolean(args[3]);

        Punishment.mute(p, p2, time, shadow);
    }

    private void mac(Player p, Player p2) {
        Punishment.macBan(p, p2);
    }

    private void reload() {
        IPMute.refreshMutes();
        IPBans.refreshBans();
    }
}
