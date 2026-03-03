package io.dm.model.inter.journal.main;

import io.dm.cache.Color;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.journal.JournalEntry;

public class AppreciationPoints extends JournalEntry {

    public static final AppreciationPoints INSTANCE = new AppreciationPoints();

    @Override
    public void send(Player player) {
        send(player, "Appreciation Points", player.appreciationPoints, Color.GREEN);
    }

    @Override
    public void select(Player player) {
        player.sendMessage("You get between 5-15 points every minute of playtime! You can spend your points on a variety of items. You currently have "
                + player.appreciationPoints + " appreciate points.");
    }

}