package io.dm.model.inter.journal.main;

import io.dm.cache.Color;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.journal.JournalEntry;

public class RefundedCredits extends JournalEntry {

    public static final RefundedCredits INSTANCE = new RefundedCredits();

    @Override
    public void send(Player player) {
        send(player, "Refunded Credits", player.refundedCredits, Color.GREEN);
    }

    @Override
    public void select(Player player) {
        player.sendMessage("Speak to the Credits Manager to claim credits. You currently have "
                + player.refundedCredits + " credits.");
    }

}