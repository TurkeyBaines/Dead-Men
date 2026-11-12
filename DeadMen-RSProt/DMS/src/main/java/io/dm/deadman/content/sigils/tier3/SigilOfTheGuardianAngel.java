package io.dm.deadman.content.sigils.tier3;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfTheGuardianAngel extends Sigil {

    @Override
    public String name() {
        return "Sigil of the Guardian Angel";
    }

    @Override
    public String shortName() {
        return "Guardian Angel";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26147;
    }

    @Override
    public int attuned() {
        return 26146;
    }

    public SigilOfTheGuardianAngel() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
