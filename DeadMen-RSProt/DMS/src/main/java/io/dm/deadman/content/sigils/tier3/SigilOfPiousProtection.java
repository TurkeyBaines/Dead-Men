package io.dm.deadman.content.sigils.tier3;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfPiousProtection extends Sigil {

    @Override
    public String name() {
        return "Sigil of Pious Protection";
    }

    @Override
    public String shortName() {
        return "Pious Protection";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26129;
    }

    @Override
    public int attuned() {
        return 26128;
    }

    public SigilOfPiousProtection() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
