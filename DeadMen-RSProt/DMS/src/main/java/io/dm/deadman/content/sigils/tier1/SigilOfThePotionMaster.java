package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.item.actions.ItemAction;

public class SigilOfThePotionMaster extends Sigil {

    @Override
    public String name() {
        return "Sigil of the Potion Master";
    }

    @Override
    public String shortName() {
        return "Potion Master";
    }

    @Override
    protected int chance() {
        return 5;
    }

    @Override
    public int unattuned() {
        return 26045;
    }

    @Override
    public int attuned() {
        return 26044;
    }

    public SigilOfThePotionMaster() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
