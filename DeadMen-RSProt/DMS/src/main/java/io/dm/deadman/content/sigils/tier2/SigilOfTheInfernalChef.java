package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.item.actions.ItemAction;
import io.dm.model.skills.cooking.Food;

public class SigilOfTheInfernalChef extends Sigil {

    @Override
    public String name() {
        return "Sigil of the Infernal Chef";
    }

    @Override
    public String shortName() {
        return "Infernal Chef";
    }

    @Override
    protected int chance() {
        return 50;
    }

    @Override
    public int unattuned() {
        return 28502;
    }

    @Override
    public int attuned() {
        return 28501;
    }

    @Override
    public boolean effect(Player p, Object... params) {
        if (!(params[0] instanceof Item)) return false;
        Item item = (Item) params[0];

        for (Food food : Food.values()) {
            if (food.rawID == item.getId()) {
                p.getInventory().addOrDrop(food.cookedID, 1);
                return true;
            }
        }

        return false;
    }

    public SigilOfTheInfernalChef() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
