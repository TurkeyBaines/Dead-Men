package io.dm.deadman;

import io.dm.cache.ItemID;
import io.dm.model.inter.dialogue.MessageDialogue;
import io.dm.model.inter.dialogue.YesNoDialogue;
import io.dm.model.item.Item;
import io.dm.model.item.actions.ItemAction;

public class TournamentItems {

    private static int TOURNAMENT_TICKET = ItemID.TOURNAMENT_TICKET;
    private static int STARTER_KIT = ItemID.STARTER_KIT;

    static {

        ItemAction.registerInventory(TOURNAMENT_TICKET, "Info", (p, i) -> {
            Item ticket = p.getInventory().findItem(TOURNAMENT_TICKET);
            if (ticket == null) return;
            p.dialogue(
                        new MessageDialogue("This ticket allows you to control the next tournament"),
                        new YesNoDialogue("Would you like to begin now?", "Yes", ticket, () -> {
                            //TODO - add tournament interface
                            new MessageDialogue("This is still yet to be implemented.");
                        })
                    );
        });

        ItemAction.registerInventory(TOURNAMENT_TICKET, "Claim", (p, i) -> {
            Item ticket = p.getInventory().findItem(TOURNAMENT_TICKET);
            if (ticket == null) return;
            p.dialogue(new MessageDialogue("This is still yet to be implemented."));
        });

    }

}
