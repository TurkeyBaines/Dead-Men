package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.item.actions.ItemAction;

public class SigilOfTheMeticulousMage extends Sigil {
    @Override
    public String name() {
        return "Sigil of the Meticulous Mage";
    }

    @Override
    public String shortName() {
        return "Meticulous Mage";
    }

    @Override
    protected int chance() {
        return 50;
    }

    @Override
    public int unattuned() {
        return 26003;
    }

    @Override
    public int attuned() {
        return 26002;
    }

    public SigilOfTheMeticulousMage() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
