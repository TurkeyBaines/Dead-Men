package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.item.actions.ItemAction;

public class SigilOfTheFoodMaster extends Sigil {

    @Override
    public String name() {
        return "Sigil of the Food Master";
    }

    @Override
    public String shortName() {
        return "Food Master";
    }

    @Override
    protected int chance() {
        return 5;
    }

    @Override
    public int unattuned() {
        return 28496;
    }

    @Override
    public int attuned() {
        return 28495;
    }

    public SigilOfTheFoodMaster() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
