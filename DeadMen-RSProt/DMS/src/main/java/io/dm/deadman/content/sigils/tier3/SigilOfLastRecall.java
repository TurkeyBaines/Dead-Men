package io.dm.deadman.content.sigils.tier3;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.item.actions.ItemAction;

public class SigilOfLastRecall extends Sigil {

    @Override
    public String name() {
        return "Sigil of Last Recall";
    }

    @Override
    public String shortName() {
        return "Last Recall";
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 26144;
    }

    @Override
    public int attuned() {
        return 26143;
    }

    public SigilOfLastRecall() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
