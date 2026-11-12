package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfSupremeStamina extends Sigil {

    @Override
    public String name() {
        return "Sigil of Supreme Stamina";
    }

    @Override
    public String shortName() {
        return "Supreme Stamina";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26120;
    }

    @Override
    public int attuned() {
        return 26119;
    }

    public SigilOfSupremeStamina() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
