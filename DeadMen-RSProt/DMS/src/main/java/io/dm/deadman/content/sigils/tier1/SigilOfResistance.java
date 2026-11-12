package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.entity.player.Player;
import io.dm.model.item.actions.ItemAction;

public class SigilOfResistance extends Sigil {
    @Override
    public String name() {
        return "Sigil of Resistance";
    }

    @Override
    public String shortName() {
        return "Resistance";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 28490;
    }

    @Override
    public int attuned() {
        return 28489;
    }

    @Override
    public boolean effect(Player p, Object... params) {



        return true;
    }

    public SigilOfResistance() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
