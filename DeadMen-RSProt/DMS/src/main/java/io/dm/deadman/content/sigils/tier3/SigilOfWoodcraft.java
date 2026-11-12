package io.dm.deadman.content.sigils.tier3;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfWoodcraft extends Sigil {

    @Override
    public String name() {
        return "Sigil of Woodcraft";
    }

    @Override
    public String shortName() {
        return "Woodcraft";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 28529;
    }

    @Override
    public int attuned() {
        return 28528;
    }

    public SigilOfWoodcraft() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
