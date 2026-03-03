package io.dm.model.inter.journal.toggles;

import io.dm.cache.Color;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.journal.JournalEntry;

public class HideYells extends JournalEntry {

    @Override
    public void send(Player player) {
        if(player.yellFilter)
            send(player, "Hide Yells", "Enabled", Color.GREEN);
        else
            send(player, "Hide Yells", "Disabled", Color.RED);
    }

    @Override
    public void select(Player player) {
        player.yellFilter = !player.yellFilter;
        send(player);
    }
}