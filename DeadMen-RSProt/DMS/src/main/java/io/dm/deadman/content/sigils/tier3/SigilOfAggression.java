package io.dm.deadman.content.sigils.tier3;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfAggression extends Sigil {

    @Override
    public String name() {
        return "Sigil of Aggression";
    }

    @Override
    public String shortName() {
        return "Aggression";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26132;
    }

    @Override
    public int attuned() {
        return 26131;
    }

    public SigilOfAggression() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
