package io.dm.model.inter.journal.main;

import io.dm.cache.Color;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.journal.JournalEntry;
import io.dm.model.inter.utils.Config;

public class TotalDeaths extends JournalEntry {

    @Override
    public void send(Player player) {
        send(player, "Total Deaths", Config.PVP_DEATHS.get(player), Color.GREEN);
    }

    @Override
    public void select(Player player) {
        KillDeathRatio.shout(player);
    }

}