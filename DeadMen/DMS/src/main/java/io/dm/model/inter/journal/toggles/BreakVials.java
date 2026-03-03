package io.dm.model.inter.journal.toggles;

import io.dm.cache.Color;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.journal.JournalEntry;

public class BreakVials extends JournalEntry {

    @Override
    public void send(Player player) {
        if(player.breakVials)
            send(player, "Break Vials", "Enabled", Color.GREEN);
        else
            send(player, "Break Vials", "Disabled", Color.RED);
    }

    @Override
    public void select(Player player) {
        player.breakVials = !player.breakVials;
        send(player);
    }

}