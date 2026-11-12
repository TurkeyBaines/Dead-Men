package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.item.actions.ItemAction;

public class SigilOfSustenance extends Sigil {

    @Override
    public String name() {
        return "Sigil of Sustenance";
    }

    @Override
    public String shortName() {
        return "Sustenance";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 28478;
    }

    @Override
    public int attuned() {
        return 28477;
    }

    public SigilOfSustenance() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
