package io.dm.model.inter.journal.main;

import io.dm.cache.Color;
import io.dm.model.activities.wilderness.Wilderness;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.journal.JournalEntry;

public class WildernessCount extends JournalEntry {

    public static final WildernessCount INSTANCE = new WildernessCount();

    @Override
    public void send(Player player) {
        send(player, "Players in Wilderness", Wilderness.players.size(), Color.GREEN);
    }

    @Override
    public void select(Player player) {
        player.sendMessage("There are currently " + Wilderness.players.size() + " players in the wilderness.");
    }

}