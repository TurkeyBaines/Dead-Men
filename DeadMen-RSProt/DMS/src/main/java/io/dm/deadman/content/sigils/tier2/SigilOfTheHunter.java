package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfTheHunter extends Sigil {

    @Override
    public String name() {
        return "Sigil of the Hunter";
    }

    @Override
    public String shortName() {
        return "Hunter";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 28487;
    }

    @Override
    public int attuned() {
        return 28486;
    }

    public SigilOfTheHunter() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
