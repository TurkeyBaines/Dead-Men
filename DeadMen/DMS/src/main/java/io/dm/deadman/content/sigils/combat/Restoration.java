package io.dm.deadman.content.sigils.combat;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.model.entity.player.Player;

public class Restoration extends Sigil {
    @Override
    public int ID() {
        return 6;
    }

    @Override
    public int Chance() {
        return 100;
    }

    @Override
    public int Cooldown() {
        return 0;
    }

    @Override
    public String name() {
        return "Restoration";
    }

    @Override
    public boolean effect(Player player, int params) {
        int dmg = params;
        double heal = dmg / 10;
        if (heal < 1.0) {
            heal = 1;
        }
        player.setHp(player.getHp() + (int) heal);
        player.sendMessage("Your Sigil of Restoration heals you for " + (int) heal + " Hitpoints.");
        return true;
    }
}
