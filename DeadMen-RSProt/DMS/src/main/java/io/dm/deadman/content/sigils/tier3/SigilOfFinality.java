package io.dm.deadman.content.sigils.tier3;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfFinality extends Sigil {

    @Override
    public String name() {
        return "Sigil of Finality";
    }

    @Override
    public String shortName() {
        return "Finality";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26126;
    }

    @Override
    public int attuned() {
        return 26125;
    }

    public SigilOfFinality() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
