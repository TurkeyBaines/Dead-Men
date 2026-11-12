package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.item.actions.ItemAction;

public class SigilOfTheAlchemaniac extends Sigil {

    @Override
    public String name() {
        return "Sigil of the Alchemaniac";
    }

    @Override
    public String shortName() {
        return "Alchemaniac";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 28484;
    }

    @Override
    public int attuned() {
        return 28483;
    }

    public SigilOfTheAlchemaniac() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
