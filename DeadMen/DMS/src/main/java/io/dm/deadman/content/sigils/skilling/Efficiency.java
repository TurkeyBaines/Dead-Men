package io.dm.deadman.content.sigils.skilling;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;

public class Efficiency extends Sigil {
    @Override
    public int ID() {
        return 25;
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
        return "Efficiency";
    }

    @Override
    public boolean effect(Player player, Item item){
        if (isActionSuccessful()) {
            player.getInventory().addOrDrop(item);
            player.getPacketSender().sendMessage("Your Sigil of Efficiency duplicated one of your resources.", "", 0);
        }

        return false;
    }
}
