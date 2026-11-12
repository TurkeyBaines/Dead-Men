package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.item.actions.ItemAction;

public class SigilOfTheForager extends Sigil {

    @Override
    public String name() {
        return "Sigil of the Forager";
    }

    @Override
    public String shortName() {
        return "Forager";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26102;
    }

    @Override
    public int attuned() {
        return 26101;
    }

    public SigilOfTheForager() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
