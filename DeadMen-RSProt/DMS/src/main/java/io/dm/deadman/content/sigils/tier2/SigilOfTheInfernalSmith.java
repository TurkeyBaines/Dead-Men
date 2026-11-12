package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfTheInfernalSmith extends Sigil {

    @Override
    public String name() {
        return "Sigil of the Infernal Smith";
    }

    @Override
    public String shortName() {
        return "Infernal Smith";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 28505;
    }

    @Override
    public int attuned() {
        return 28504;
    }

    public SigilOfTheInfernalSmith() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
