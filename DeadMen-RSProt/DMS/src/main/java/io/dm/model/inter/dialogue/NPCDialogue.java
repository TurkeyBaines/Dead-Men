package io.dm.model.inter.dialogue;

import io.dm.cache.NPCDef;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceHandler;
import io.dm.model.inter.InterfaceType;
import io.dm.model.inter.actions.SimpleAction;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NPCDialogue implements Dialogue {

    private NPCDef npcDef;

    private String message;

    private Runnable onDialogueOpened;

    private int animationId = 554;

    private int lineHeight;

    private boolean hideContinue;

    public NPCDialogue(int npcId, String message) {
        this(NPCDef.get(npcId), message);
    }

    public NPCDialogue(NPC npc, String message) {
        this(npc.getDef(), message);
    }

    private NPCDialogue(NPCDef npcDef, String message) {
        this.npcDef = npcDef;
        this.message = message;
    }

    public NPCDialogue onDialogueOpened(Runnable openDialogueOpened) {
        this.onDialogueOpened = openDialogueOpened;
        return this;
    }

    public NPCDialogue animate(int animationId) {
        this.animationId = animationId;
        return this;
    }

    public NPCDialogue lineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }

    public NPCDialogue hideContinue() {
        this.hideContinue = true;
        return this;
    }

    @Override
    public void open(Player player) {
        // Rev-228 child layout (Stage 11 Round 2): head=2, name=4, continue=5, message=6 — per
        // AlterRSPS chatNpc() (QueueTaskExt.kt, incl. its settextalign target 15138822 = 231:6) and
        // this server's own PROTOCOL_DUMP.txt (the client natively sends ResumePauseButton on 231:5).
        // The previous 1/2/3/4 were the 184-era children: the head/name landed on dead components
        // and the message rendered in the NAME slot (red, top-centre).
        player.openInterface(InterfaceType.CHATBOX, Interface.NPC_DIALOGUE);
        player.getPacketSender().sendNpcHead(Interface.NPC_DIALOGUE, 2, npcDef.id);
        player.getPacketSender().animateInterface(Interface.NPC_DIALOGUE, 2, animationId);
        player.getPacketSender().sendString(Interface.NPC_DIALOGUE, 4, npcDef.name);
        player.getPacketSender().sendString(Interface.NPC_DIALOGUE, 6, message);
        player.getPacketSender().setTextStyle(Interface.NPC_DIALOGUE, 6, 1, 1, lineHeight);
        player.getPacketSender().sendAccessMask(Interface.NPC_DIALOGUE, 5, -1, -1, 1);
        if (hideContinue)
            player.getPacketSender().setHidden(Interface.NPC_DIALOGUE, 5, true);
        else
            player.getPacketSender().sendString(Interface.NPC_DIALOGUE, 5, "Click here to continue");
        if (onDialogueOpened != null)
            onDialogueOpened.run();
    }

    static {
        InterfaceHandler.register(Interface.NPC_DIALOGUE, h -> h.actions[5] = (SimpleAction) player -> {
            player.continueDialogue();
            player.onDialogueContinued();
        });
    }

}
