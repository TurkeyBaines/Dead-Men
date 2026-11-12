package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.item.actions.ItemAction;

public class SigilOfTheEternalJeweller extends Sigil {

    @Override
    public String name() {
        return "Sigil of the Eternal Jeweller";
    }

    @Override
    public String shortName() {
        return "Eternal Jeweller";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26048;
    }

    @Override
    public int attuned() {
        return 26047;
    }

    public SigilOfTheEternalJeweller() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
