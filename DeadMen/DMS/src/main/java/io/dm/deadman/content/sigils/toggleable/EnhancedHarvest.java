package io.dm.deadman.content.sigils.toggleable;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;

public class EnhancedHarvest extends Sigil {
    @Override
    public int ID() {
        return 1;
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
        return "Enhanced Harvest";
    }

    @Override
    public boolean effect(Player player, Item item) {
        //Check for Sigil of Remote Storage <> Send to Bank if held!

        player.getInventory().addOrDrop(item);
        player.getInventory().addOrDrop(item);
        player.getInventory().addOrDrop(item);

        return true;
    }
}
