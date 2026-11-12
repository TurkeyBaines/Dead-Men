package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.item.actions.ItemAction;

public class SigilOfDeftStrikes extends Sigil {

    @Override
    public String name() {
        return "Sigil of Deft Strikes";
    }

    @Override
    public String shortName() {
        return "Deft Strikes";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26012;
    }

    @Override
    public int attuned() {
        return 26011;
    }

    public SigilOfDeftStrikes() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
