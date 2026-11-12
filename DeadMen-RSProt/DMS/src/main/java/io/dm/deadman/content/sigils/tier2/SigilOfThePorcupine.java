package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfThePorcupine extends Sigil {

    @Override
    public String name() {
        return "Sigil of the Porcupine";
    }

    @Override
    public String shortName() {
        return "Porcupine";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26063;
    }

    @Override
    public int attuned() {
        return 26062;
    }

    public SigilOfThePorcupine() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
