package io.dm.model.inter.journal.main;

import io.dm.cache.Color;
import io.dm.model.World;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.MessageDialogue;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.journal.JournalEntry;
import io.dm.model.inter.utils.Option;

public class TotalSpent extends JournalEntry {

    public static final TotalSpent INSTANCE = new TotalSpent();

    @Override
    public void send(Player player) {
        double spentDollars = player.storeAmountSpent;
        send(player, "Total Spent", "$" + spentDollars, Color.GREEN);
    }

    @Override
    public void select(Player player) {
        double spentDollars = player.storeAmountSpent;
        player.dialogue(
                new MessageDialogue("You have spent a total of " + Color.COOL_BLUE.wrap("$" + spentDollars) +
                " inside the store. You can view a list of donator benefits by going to "
                        + Color.COOL_BLUE.wrap(World.type.getWebsiteUrl() + "/donator") + "."),
                new OptionsDialogue("View the " + World.type.getWorldName() + " Donator Benefits?",
                        new Option("Yes", () -> player.openUrl(World.type.getWorldName() + " Donator Benefits", World.type.getWebsiteUrl() + "/donator")),
                        new Option("No", player::closeDialogue)));
    }

}