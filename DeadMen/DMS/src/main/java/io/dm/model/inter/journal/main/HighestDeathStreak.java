package io.dm.model.inter.journal.main;

import io.dm.cache.Color;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.journal.JournalEntry;

public class HighestDeathStreak extends JournalEntry {

    @Override
    public void send(Player player) {
        send(player, "Highest Death Streak", "15", Color.GREEN);
    }

    @Override
    public void select(Player player) {
    }

}