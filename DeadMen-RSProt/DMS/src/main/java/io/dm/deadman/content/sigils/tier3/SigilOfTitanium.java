package io.dm.deadman.content.sigils.tier3;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfTitanium extends Sigil {

    @Override
    public String name() {
        return "Sigil of Titanium";
    }

    @Override
    public String shortName() {
        return "Titanium";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 28523;
    }

    @Override
    public int attuned() {
        return 28522;
    }

    public SigilOfTitanium() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
