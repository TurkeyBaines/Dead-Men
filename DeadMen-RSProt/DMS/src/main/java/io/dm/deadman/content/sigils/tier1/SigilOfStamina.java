package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.item.actions.ItemAction;

public class SigilOfStamina extends Sigil {

    @Override
    public String name() {
        return "Sigil of Stamina";
    }

    @Override
    public String shortName() {
        return "Stamina";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26042;
    }

    @Override
    public int attuned() {
        return 26041;
    }

    public SigilOfStamina() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
