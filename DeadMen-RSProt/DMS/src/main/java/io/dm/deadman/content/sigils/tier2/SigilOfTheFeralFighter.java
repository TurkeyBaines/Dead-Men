package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfTheFeralFighter extends Sigil {

    @Override
    public String name() {
        return "Sigil of the Feral Fighter";
    }

    @Override
    public String shortName() {
        return "Feral Fighter";
    }

    @Override
    protected int chance() {
        return 20;
    }

    @Override
    public int unattuned() {
        return 26075;
    }

    @Override
    public int attuned() {
        return 26074;
    }

    public SigilOfTheFeralFighter() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
