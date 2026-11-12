package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.item.actions.ItemAction;

public class SigilOfEnhancedHarvest extends Sigil {

    @Override
    public String name() {
        return "Sigil of Enhanced Harvest";
    }

    @Override
    public String shortName() {
        return "Enhanced Harvest";
    }

    @Override
    protected int chance() {
        return 100;
    }

    @Override
    public int unattuned() {
        return 26018;
    }

    @Override
    public int attuned() {
        return 26017;
    }

    @Override
    public boolean effect(Player p, Object... params) {
        if (!(params[0] instanceof Item)) return false;
        Item item = (Item) params[0];

        p.getInventory().addOrDrop(item);
        p.getInventory().addOrDrop(item);
        p.getInventory().addOrDrop(item);

        return true;
    }

    public SigilOfEnhancedHarvest() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
