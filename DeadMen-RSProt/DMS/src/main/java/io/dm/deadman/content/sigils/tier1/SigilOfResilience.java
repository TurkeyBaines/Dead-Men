package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.item.actions.ItemAction;

public class SigilOfResilience extends Sigil {

    @Override
    public String name() {
        return "Sigil of Resilience";
    }

    @Override
    public String shortName() {
        return "Resilience";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 25991;
    }

    @Override
    public int attuned() {
        return 25990;
    }

    public SigilOfResilience() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
