package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfVersatility extends Sigil {

    @Override
    public String name() {
        return "Sigil of Versatility";
    }

    @Override
    public String shortName() {
        return "Versatility";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26114;
    }

    @Override
    public int attuned() {
        return 26113;
    }

    public SigilOfVersatility() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
