package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.item.actions.ItemAction;

public class SigilOfStorage extends Sigil {

    @Override
    public String name() {
        return "Sigil of Storage";
    }

    @Override
    public String shortName() {
        return "Storage";
    }

    @Override
    protected int chance() {
        return 100;
    }

    @Override
    public int unattuned() {
        return 26021;
    }

    @Override
    public int attuned() {
        return 26020;
    }

    @Override
    public boolean effect(Player p, Object... params) {
        if (!(params[0] instanceof Item)) return false;
        Item item = (Item) params[0];

        p.getBank().add(item.getId(), item.getAmount());
        p.sendFilteredMessage("Your Sigil of Storage sends the resource straight to your bank.");

        return true;
    }

    public SigilOfStorage() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
