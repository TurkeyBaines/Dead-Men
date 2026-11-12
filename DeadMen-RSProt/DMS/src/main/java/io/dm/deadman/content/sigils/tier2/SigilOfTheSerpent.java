package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfTheSerpent extends Sigil {

    @Override
    public String name() {
        return "Sigil of the Serpent";
    }

    @Override
    public String shortName() {
        return "Serpent";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26117;
    }

    @Override
    public int attuned() {
        return 26116;
    }

    public SigilOfTheSerpent() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
