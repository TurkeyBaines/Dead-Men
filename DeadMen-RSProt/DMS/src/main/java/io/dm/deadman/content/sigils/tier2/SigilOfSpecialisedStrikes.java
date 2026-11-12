package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfSpecialisedStrikes extends Sigil {

    @Override
    public String name() {
        return "Sigil of Specialised Strikes";
    }

    @Override
    public String shortName() {
        return "Specialised Strikes";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26060;
    }

    @Override
    public int attuned() {
        return 26059;
    }

    public SigilOfSpecialisedStrikes() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
