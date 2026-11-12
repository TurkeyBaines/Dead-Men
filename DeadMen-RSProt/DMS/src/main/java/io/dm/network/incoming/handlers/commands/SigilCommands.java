package io.dm.network.incoming.handlers.commands;

import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.entity.player.Player;

public class SigilCommands {

    public void process(Player player, String... args) {
        switch (args[0]) {
            case "print":
                print(player);
                break;

            case "reset":
                reset(player);
                break;

            case "remove":
                remove(player, args);
                break;
        }
    }

    public void print(Player p) {
        p.sendMessage("----==== Sigils ====----");
        for (int i = 0; i < 3; i++) {
            if (p.getSigilID(i) == -1) continue;

            p.sendMessage("[" + i + "] Sigil: " + p.getSigil(i).name());
        }
        p.sendMessage("----===============----");
    }

    public void reset(Player p) {
        p.resetSigils();
    }

    public void remove(Player p, String[] args) {
        int slot = Integer.parseInt(args[1]);
        p.remove(slot);
    }
}
