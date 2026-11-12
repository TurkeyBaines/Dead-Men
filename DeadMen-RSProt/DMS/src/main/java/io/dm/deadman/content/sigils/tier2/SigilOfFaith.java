package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.item.actions.ItemAction;
import io.dm.model.stat.Stat;
import io.dm.model.stat.StatType;

public class SigilOfFaith extends Sigil {

    @Override
    public String name() {
        return "Sigil of Faith";
    }

    @Override
    public String shortName() {
        return "Faith";
    }

    @Override
    protected int chance() {
        return 100;
    }

    @Override
    public int unattuned() {
        return 28520;
    }

    @Override
    public int attuned() {
        return 28519;
    }

    @Override
    public boolean effect(Player p, Object... params) {
        if (!(params[0] instanceof Item)) return false;
        Item item = (Item) params[0];
        String name = item.getDef().name.toLowerCase();

        Stat prayer = p.getStats().get(StatType.Prayer);
        if (name.contains("prayer potion")) {
            prayer.restore(7, 0.27);
            prayer.alter(prayer.currentLevel);
        } else if (name.contains("super restore")) {
            prayer.restore(8, 0.25);
            prayer.alter(prayer.currentLevel);
        } else {
            return false;
        }

        return true;
    }

    public SigilOfFaith() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
