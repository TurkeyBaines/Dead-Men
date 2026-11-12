package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.item.actions.ItemAction;

public class SigilOfExaggeration extends Sigil {

    @Override
    public String name() {
        return "Sigil of Exaggeration";
    }

    @Override
    public String shortName() {
        return "Exaggeration";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26057;
    }

    @Override
    public int attuned() {
        return 26056;
    }

    public SigilOfExaggeration() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
