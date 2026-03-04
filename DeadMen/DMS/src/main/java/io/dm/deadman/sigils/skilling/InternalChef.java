package io.dm.deadman.sigils.skilling;

import io.dm.deadman.sigils.Sigil;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.skills.cooking.Food;

public class InternalChef extends Sigil {
    @Override
    public int ID() {
        return 20;
    }

    @Override
    public int Chance() {
        return 50;
    }

    @Override
    public int Cooldown() {
        return 0;
    }

    @Override
    public String name() {
        return "Internal Chef";
    }

    @Override
    public boolean effect(Player player, Item item) {
        if (isActionSuccessful())
            for (Food food : Food.values()) {
                if (food.rawID == item.getId()) {
                    player.getInventory().add(food.cookedID, 1);
                    return true;
                }
            }

        return false;
    }
}
