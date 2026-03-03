package io.dm.model.entity.npc.actions.tzhaar;

import io.dm.model.entity.npc.NPCAction;
import io.dm.model.inter.dialogue.NPCDialogue;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.dialogue.PlayerDialogue;
import io.dm.model.inter.utils.Option;
import io.dm.model.shop.ShopManager;

public class TzHaarHurTel {

    static {
        NPCAction.register(2183, "talk-to", (player, npc) -> player.dialogue(
                new NPCDialogue(npc, "Can I help you JalYt-Xil-" + player.getName() + "?"),
                new OptionsDialogue(
                        new Option("What do you have to trade?", () -> ShopManager.openIfExists(player, "")),//TODO Fill this in
                        new Option("No I'm fine thanks.", () -> player.dialogue(new PlayerDialogue("No I'm fine thanks.")))
                )
        ));
    }
}
