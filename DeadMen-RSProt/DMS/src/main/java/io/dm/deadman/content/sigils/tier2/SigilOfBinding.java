package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfBinding extends Sigil {

    @Override
    public String name() {
        return "Sigil of Binding";
    }

    @Override
    public String shortName() {
        return "Binding";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26066;
    }

    @Override
    public int attuned() {
        return 26065;
    }

    public SigilOfBinding() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
