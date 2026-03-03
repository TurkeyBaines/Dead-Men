package io.dm.model.entity.npc.actions.guild.fishing;

import io.dm.model.entity.npc.NPCAction;
import io.dm.model.inter.dialogue.NPCDialogue;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.dialogue.PlayerDialogue;
import io.dm.model.inter.utils.Option;
import io.dm.model.shop.ShopManager;

public class Roachey {

    private static final int ROACHEY = 1299;

    static {
        NPCAction.register(ROACHEY, "talk-to", (player, npc) -> player.dialogue(
                new NPCDialogue(npc, "Would you like to buy some fishing equipment?"),
                new OptionsDialogue(
                        new Option("Yes please.", () -> ShopManager.openIfExists(player, "")),//TODO Fill this in
                        new Option("No thank you.", () -> player.dialogue(new PlayerDialogue("No thank you.")))
                )
        ));
    }
}
