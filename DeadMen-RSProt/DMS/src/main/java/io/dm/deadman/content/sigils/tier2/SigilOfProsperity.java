package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfProsperity extends Sigil {

    @Override
    public String name() {
        return "Sigil of Prosperity";
    }

    @Override
    public String shortName() {
        return "Prosperity";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26081;
    }

    @Override
    public int attuned() {
        return 26080;
    }

    public SigilOfProsperity() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
