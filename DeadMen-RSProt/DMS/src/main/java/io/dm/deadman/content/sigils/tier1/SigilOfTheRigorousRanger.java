package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.item.actions.ItemAction;

public class SigilOfTheRigorousRanger extends Sigil {
    @Override
    public String name() {
        return "Sigil of the Rigorous Ranger";
    }

    @Override
    public String shortName() {
        return "Rigorous Ranger";
    }

    @Override
    protected int chance() {
        return 50;
    }

    @Override
    public int unattuned() {
        return 26000;
    }

    @Override
    public int attuned() {
        return 25999;
    }

    public SigilOfTheRigorousRanger() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
