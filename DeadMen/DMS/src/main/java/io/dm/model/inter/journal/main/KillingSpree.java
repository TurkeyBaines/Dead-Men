package io.dm.model.inter.journal.main;

import io.dm.cache.Color;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.journal.JournalEntry;

public class KillingSpree extends JournalEntry {

    @Override
    public void send(Player player) {
        send(player, "Killing Spree", player.currentKillSpree, Color.GREEN);
    }

    @Override
    public void select(Player player) {
        player.forceText("!" + Color.ORANGE_RED.wrap("CURRENT KILLING SPREE:") + " " + player.currentKillSpree );
    }

}