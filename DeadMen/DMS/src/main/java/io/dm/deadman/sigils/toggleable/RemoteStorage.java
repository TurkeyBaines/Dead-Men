package io.dm.deadman.sigils.toggleable;

import io.dm.deadman.sigils.Sigil;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;

public class RemoteStorage extends Sigil {
    @Override
    public int ID() {
        return 2;
    }

    @Override
    public int Chance() {
        return 100;
    }

    @Override
    public int Cooldown() {
        return 0;
    }

    @Override
    public String name() {
        return "Remote Storage";
    }

    @Override
    public boolean effect(Player player, Item item) {
        player.getBank().add(item.getId(), item.getAmount());
        player.getPacketSender().sendMessage("Your Sigil of Remote Storage has sent your resource to the bank.", "", 0);
        return true;
    }
}
