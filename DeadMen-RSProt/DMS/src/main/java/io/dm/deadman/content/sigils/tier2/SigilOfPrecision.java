package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfPrecision extends Sigil {

    @Override
    public String name() {
        return "Sigil of Precision";
    }

    @Override
    public String shortName() {
        return "Precision";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 28514;
    }

    @Override
    public int attuned() {
        return 28513;
    }

    public SigilOfPrecision() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
