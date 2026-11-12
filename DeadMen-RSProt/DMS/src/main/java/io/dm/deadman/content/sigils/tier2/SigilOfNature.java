package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfNature extends Sigil {

    @Override
    public String name() {
        return "Sigil of Nature";
    }

    @Override
    public String shortName() {
        return "Nature";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26096;
    }

    @Override
    public int attuned() {
        return 26095;
    }

    public SigilOfNature() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
