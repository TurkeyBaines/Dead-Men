package io.dm.deadman.content.sigils.utility;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.model.entity.player.Player;

public class WellFed extends Sigil {
    @Override
    public int ID() {
        return 33;
    }

    @Override
    public int Chance() {
        return 5;
    }

    @Override
    public int Cooldown() {
        return 0;
    }

    @Override
    public String name() {
        return "Well Fed";
    }

    public boolean effect(Player player, int params) {
        if (isActionSuccessful()) {
            int current = player.getHp();
            int max = player.getMaxHp();
            int heal = params;

            if ((current + heal) > max) {
                heal = max - current;
            }

            player.setHp(current + heal);
            return true;
        }
        return false;
    }
}
