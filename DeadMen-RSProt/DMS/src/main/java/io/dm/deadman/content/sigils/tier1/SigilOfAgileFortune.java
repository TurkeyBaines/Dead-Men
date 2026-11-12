package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.item.actions.ItemAction;

public class SigilOfAgileFortune extends Sigil {

    @Override
    public String name() {
        return "Sigil of Agile Fortune";
    }

    @Override
    public String shortName() {
        return "Agile Fortune";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 28493;
    }

    @Override
    public int attuned() {
        return 28492;
    }

    public SigilOfAgileFortune() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
