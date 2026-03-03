package io.dm.model.entity.npc.actions.barrows;

import io.dm.api.utils.Random;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.npc.NPCAction;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.NPCDialogue;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.dialogue.PlayerDialogue;
import io.dm.model.inter.utils.Option;

public class StrangeOldMan {

    private static void randomChatOne(Player player, NPC npc) {
        player.dialogue(
                new NPCDialogue(npc, "AAAAAAARRRRRGGGGGGGGHHHHHHH!"),
                new OptionsDialogue(
                        new Option("What's wrong?", () -> player.dialogue(
                                new PlayerDialogue("What's wrong?"),
                                new NPCDialogue(npc, "AAAAAAARRRRRGGGGGGGGHHHHHHH!")
                        )),
                        new Option("I'll leave you to it then...", () -> new PlayerDialogue("I'll leave you to it then..."))
                )
        );
    }

    private static void randomChatTwo(Player player, NPC npc) {
        player.dialogue(
                new NPCDialogue(npc, "Knock knock."),
                new PlayerDialogue("Who's there?"),
                new NPCDialogue(npc, "A big scary monster HAHAHAHAHAHHAH!"),
                new PlayerDialogue("Okay...")
                );
    }

    private static void randomChatThree(Player player, NPC npc) {
        player.dialogue(
                new NPCDialogue(npc, "Pst, wanna hear a secret?"),
                new OptionsDialogue(
                        new Option("Sure!", () -> player.dialogue(
                                new PlayerDialogue("Sure!"),
                                new NPCDialogue(npc, "They're not normal!")
                        )),
                        new Option("No thanks.", () -> player.dialogue(
                                new PlayerDialogue("No thanks.")
                        ))
                )
        );
    }

    private static void randomChatFour(Player player, NPC npc) {
        player.dialogue(new NPCDialogue(npc, "Dig, dig, dig."));
    }

    static {
        NPCAction.register(1671, "talk-to", (player, npc) -> {
            int randomChat = Random.get(1, 4);
            if(randomChat == 1)
                randomChatOne(player, npc);
            if(randomChat == 2)
                randomChatTwo(player, npc);
            if(randomChat == 3)
                randomChatThree(player, npc);
            if(randomChat == 4)
                randomChatFour(player, npc);
        });
    }
}
