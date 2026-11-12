package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.item.actions.ItemAction;

public class SigilOfConsistency extends Sigil {

    @Override
    public String name() {
        return "Sigil of Consistency";
    }

    @Override
    public String shortName() {
        return "Consistency";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 25994;
    }

    @Override
    public int attuned() {
        return 25993;
    }

    public SigilOfConsistency() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
