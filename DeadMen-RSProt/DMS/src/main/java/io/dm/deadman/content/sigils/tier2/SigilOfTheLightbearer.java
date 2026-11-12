package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfTheLightbearer extends Sigil {

    @Override
    public String name() {
        return "Sigil of the Lightbearer";
    }

    @Override
    public String shortName() {
        return "Lightbearer";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 28508;
    }

    @Override
    public int attuned() {
        return 28507;
    }

    public SigilOfTheLightbearer() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
