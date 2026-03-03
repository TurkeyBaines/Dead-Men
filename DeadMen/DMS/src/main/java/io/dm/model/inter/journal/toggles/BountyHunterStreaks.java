package io.dm.model.inter.journal.toggles;

import io.dm.cache.Color;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.journal.JournalEntry;
import io.dm.model.inter.utils.Config;

public class BountyHunterStreaks extends JournalEntry {

    @Override
    public void send(Player player) {
        send(player, Config.BOUNTY_HUNTER_RECORD_OVERLAY.get(player) == 0);
    }

    private void send(Player player, boolean enabled) {
        if(enabled)
            send(player, "Streaks Overlay", "On", Color.GREEN);
        else
            send(player, "Streaks Overlay", "Off", Color.RED);
    }

    @Override
    public void select(Player player) {
        send(player, Config.BOUNTY_HUNTER_RECORD_OVERLAY.toggle(player) == 0);
    }

}