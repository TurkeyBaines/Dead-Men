package io.dm.deadman.sigils.utility;

import io.dm.deadman.sigils.Sigil;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.skills.cooking.Food;
import io.dm.model.stat.Stat;
import io.dm.model.stat.StatType;

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
