package io.dm.model.inter.journal.main;

import io.dm.cache.Color;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.journal.JournalEntry;

public class CurrentDeathStreak extends JournalEntry {

    @Override
    public void send(Player player) {
        send(player, "Current Death Streak", "0", Color.GREEN);
    }

    @Override
    public void select(Player player) {
    }

}