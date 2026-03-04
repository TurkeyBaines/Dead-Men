package io.dm.deadman.sigils.utility;

import io.dm.cache.ItemID;
import io.dm.deadman.sigils.Sigil;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.item.containers.Equipment;
import io.dm.model.stat.Stat;
import io.dm.model.stat.StatType;

public class Faith extends Sigil {
    @Override
    public int ID() {
        return 30;
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
        return "Faith";
    }

    @Override
    public boolean effect(Player player, Item item) {
        int base = player.getStats().get(StatType.Prayer).fixedLevel;
        if (item.getDef().name.toLowerCase().contains("prayer potion")) {
            Stat stat = player.getStats().get(StatType.Prayer);
            stat.restore(7, 0.27);
            player.getStats().get(StatType.Prayer).alter(stat.currentLevel);
        } else if (item.getDef().name.toLowerCase().contains("super restore")) {
            Stat stat = player.getStats().get(StatType.Prayer);
            stat.restore(8, 0.25);
            player.getStats().get(StatType.Prayer).alter(stat.currentLevel);
        }

        return true;
    }
}
