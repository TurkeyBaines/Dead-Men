package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfSlaughter extends Sigil {

    @Override
    public String name() {
        return "Sigil of Slaughter";
    }

    @Override
    public String shortName() {
        return "Slaughter";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26108;
    }

    @Override
    public int attuned() {
        return 26107;
    }

    public SigilOfSlaughter() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
