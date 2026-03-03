package io.dm.model.inter.journal.main;

import io.dm.cache.Color;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.journal.JournalEntry;

public class HighestKillingSpree extends JournalEntry {

    @Override
    public void send(Player player) {
        send(player, "Highest Killing Spree", player.highestKillSpree, Color.GREEN);
    }

    @Override
    public void select(Player player) {
        player.forceText("!" + Color.ORANGE_RED.wrap("HIGHEST KILLING SPREE:") + " " + player.highestKillSpree );
    }

}