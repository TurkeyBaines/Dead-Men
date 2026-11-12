package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.item.actions.ItemAction;

public class SigilOfMobility extends Sigil {

    @Override
    public String name() {
        return "Sigil of Mobility";
    }

    @Override
    public String shortName() {
        return "Mobility";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26054;
    }

    @Override
    public int attuned() {
        return 26053;
    }

    public SigilOfMobility() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
