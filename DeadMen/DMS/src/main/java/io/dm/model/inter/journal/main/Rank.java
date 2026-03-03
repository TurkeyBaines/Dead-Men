package io.dm.model.inter.journal.main;

import io.dm.cache.Color;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.journal.JournalEntry;

public class Rank extends JournalEntry {

    @Override
    public void send(Player player) {
        send(player, "Rank", "<img=1>Administrator", Color.YELLOW);
    }

    @Override
    public void select(Player player) {
    }

}