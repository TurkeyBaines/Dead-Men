package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfFortification extends Sigil {

    @Override
    public String name() {
        return "Sigil of Fortification";
    }

    @Override
    public String shortName() {
        return "Fortification";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26006;
    }

    @Override
    public int attuned() {
        return 26005;
    }

    public SigilOfFortification() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
