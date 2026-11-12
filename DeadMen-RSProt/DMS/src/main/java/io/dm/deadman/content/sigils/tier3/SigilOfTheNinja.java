package io.dm.deadman.content.sigils.tier3;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfTheNinja extends Sigil {

    @Override
    public String name() {
        return "Sigil of the Ninja";
    }

    @Override
    public String shortName() {
        return "Ninja";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 28526;
    }

    @Override
    public int attuned() {
        return 28525;
    }

    public SigilOfTheNinja() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
