package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.entity.player.Player;
import io.dm.model.item.actions.ItemAction;
import io.dm.model.stat.StatType;

public class SigilOfDevotion extends Sigil {

    @Override
    public String name() {
        return "Sigil of Devotion";
    }

    @Override
    public String shortName() {
        return "Devotion";
    }

    @Override
    protected int chance() {
        return 100;
    }

    @Override
    public int unattuned() {
        return 26099;
    }

    @Override
    public int attuned() {
        return 26098;
    }

    @Override
    public boolean effect(Player p, Object... params) {
        double baseXp = ((Number) params[0]).doubleValue();

        p.getStats().get(StatType.Prayer).experience += (baseXp * 2);
        p.getStats().get(StatType.Prayer).updated = true;

        return true;
    }

    public SigilOfDevotion() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
