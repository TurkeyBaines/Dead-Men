package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.item.actions.ItemAction;

public class SigilOfTheAbyss extends Sigil {

    @Override
    public String name() {
        return "Sigil of the Abyss";
    }

    @Override
    public String shortName() {
        return "Abyss";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26039;
    }

    @Override
    public int attuned() {
        return 26038;
    }

    public SigilOfTheAbyss() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
