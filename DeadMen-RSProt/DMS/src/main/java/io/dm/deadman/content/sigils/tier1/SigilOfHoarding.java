package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.item.actions.ItemAction;

public class SigilOfHoarding extends Sigil {

    @Override
    public String name() {
        return "Sigil of Hoarding";
    }

    @Override
    public String shortName() {
        return "Hoarding";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 28481;
    }

    @Override
    public int attuned() {
        return 28480;
    }

    public SigilOfHoarding() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
