package io.dm.model.inter.journal.main;

import io.dm.cache.Color;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.journal.JournalEntry;
import io.dm.model.inter.utils.Config;

public class TotalKills extends JournalEntry {

    @Override
    public void send(Player player) {
        send(player, "Total Kills", Config.PVP_KILLS.get(player), Color.GREEN);
    }

    @Override
    public void select(Player player) {
        KillDeathRatio.shout(player);
    }

}