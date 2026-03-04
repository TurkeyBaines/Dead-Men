package io.dm.deadman.sigils.toggleable;

import io.dm.deadman.sigils.Sigil;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.stat.StatType;

public class Devotion extends Sigil {
    @Override
    public int ID() {
        return 0;
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
        return "Devotion";
    }

    @Override
    public boolean effect(Player player, Item item, int param) {
        player.getStats().get(StatType.Prayer).experience += (param * 2);
        player.getStats().get(StatType.Prayer).updated = true;

        return true;
    }
}
