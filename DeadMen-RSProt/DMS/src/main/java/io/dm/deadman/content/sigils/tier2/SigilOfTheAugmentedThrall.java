package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfTheAugmentedThrall extends Sigil {

    @Override
    public String name() {
        return "Sigil of the Augmented Thrall";
    }

    @Override
    public String shortName() {
        return "Augmented Thrall";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 28517;
    }

    @Override
    public int attuned() {
        return 28516;
    }

    public SigilOfTheAugmentedThrall() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
