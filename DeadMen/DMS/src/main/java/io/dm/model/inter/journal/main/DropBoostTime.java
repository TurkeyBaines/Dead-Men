package io.dm.model.inter.journal.main;

import io.dm.cache.Color;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.journal.JournalEntry;

public class DropBoostTime extends JournalEntry {

    public static final DropBoostTime INSTANCE = new DropBoostTime();

    @Override
    public void send(Player player) {
        if(player.rareDropBonus.isDelayed()) {
            int minutes = player.rareDropBonus.remaining() / 100;
            if(minutes == 0)
                send(player, "Rare Drop Boost", "Expiring soon!", Color.ORANGE);
                else
            send(player, "Rare Drop Boost", minutes + (minutes == 1 ? " minute" : " minutes"), Color.GREEN);
        } else {
            send(player, "Rare Drop Boost", "Inactive", Color.RED);
        }
    }

    @Override
    public void select(Player player) {
        if(player.rareDropBonus.isDelayed()) {
            player.sendMessage("Your rare drops are currently being boosted by 15%.");
        } else {
            player.sendMessage("You do not have a rare drop boost activated.");
        }
    }

}