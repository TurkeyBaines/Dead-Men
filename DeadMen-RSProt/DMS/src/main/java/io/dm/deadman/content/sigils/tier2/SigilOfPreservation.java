package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfPreservation extends Sigil {

    @Override
    public String name() {
        return "Sigil of Preservation";
    }

    @Override
    public String shortName() {
        return "Preservation";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26123;
    }

    @Override
    public int attuned() {
        return 26122;
    }

    public SigilOfPreservation() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
