package io.dm.model.inter.journal.main;

import io.dm.cache.Color;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.journal.JournalEntry;

public class SlayerInfo extends JournalEntry {

    @Override
    public void send(Player player) {
        send(player, "Slayer Info", "[ ? ]", Color.TOMATO);
    }

    @Override
    public void select(Player player) {
        //todo - send task name
        //todo - send task remaining
        //todo - send slayer points
    }

}