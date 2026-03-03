package io.dm.model.inter.journal.bestiary;

import io.dm.cache.Color;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.journal.JournalEntry;

public class BestiarySearchMonster extends JournalEntry {

    public static final BestiarySearchMonster INSTANCE = new BestiarySearchMonster();

    @Override
    public void send(Player player) {
        send(player, "<img=108> Search for Monster", Color.ORANGE);
    }

    @Override
    public void select(Player player) {
        player.stringInput("<img=108> Enter monster name to search for:", name -> Bestiary.search(player, name, true));
    }

}