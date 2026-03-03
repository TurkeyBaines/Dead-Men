package io.dm.model.entity.npc.actions.wintertodt;

import io.dm.model.entity.npc.NPCAction;
import io.dm.model.inter.dialogue.NPCDialogue;

public class Cat {

    static {
        NPCAction.register(7380, "talk-to", (player, npc) -> player.dialogue(new NPCDialogue(npc, "Meow.")));
    }
}
