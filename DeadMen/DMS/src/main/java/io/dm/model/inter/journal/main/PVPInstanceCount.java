package io.dm.model.inter.journal.main;

import io.dm.cache.Color;
import io.dm.model.activities.pvp.PVPInstance;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.journal.JournalEntry;

public class PVPInstanceCount extends JournalEntry {

    public static final PVPInstanceCount INSTANCE = new PVPInstanceCount();

    @Override
    public void send(Player player) {
        send(player, "Players in PVP Instances", PVPInstance.players.size(), Color.GREEN);
    }

    @Override
    public void select(Player player) {
        player.sendMessage("There are currently " + PVPInstance.players.size() + " players in PVP Instances.");
        player.sendMessage(Color.DARK_RED.wrap("::cammypvp, ::fallypvp, ::lumbpvp"));
    }

}