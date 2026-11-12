package io.dm.deadman.content.sigils.tier3;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfRemoteStorage extends Sigil {

    @Override
    public String name() {
        return "Sigil of Remote Storage";
    }

    @Override
    public String shortName() {
        return "Remote Storage";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26141;
    }

    @Override
    public int attuned() {
        return 26140;
    }

    public SigilOfRemoteStorage() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
