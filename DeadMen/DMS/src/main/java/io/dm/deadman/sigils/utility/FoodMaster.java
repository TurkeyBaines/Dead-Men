package io.dm.deadman.sigils.utility;

import io.dm.deadman.sigils.Sigil;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;

public class FoodMaster extends Sigil {
    @Override
    public int ID() {
        return 31;
    }

    @Override
    public int Chance() {
        return 5;
    }

    @Override
    public int Cooldown() {
        return 10;
    }

    @Override
    public String name() {
        return "Food Master";
    }

    @Override
    public boolean effect(Player player, Item item) {
        if (isActionSuccessful()) {
            return true;
        }

        return false;
    }
}
