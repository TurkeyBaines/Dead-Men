package io.dm.model.inter.dialogue;

import io.dm.model.entity.player.Player;
import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceHandler;
import io.dm.model.inter.InterfaceType;
import io.dm.model.inter.actions.SimpleAction;

public class PlayerDialogue implements Dialogue {

    private String message;

    private Runnable action;

    private int animationId = 554;

    private int lineHeight;

    private boolean hideContinue;

    public PlayerDialogue(String message) {
        this.message = message;
    }

    public PlayerDialogue action(Runnable action) {
        this.action = action;
        return this;
    }

    public PlayerDialogue animate(int animationId) {
        this.animationId = animationId;
        return this;
    }

    public PlayerDialogue lineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }

    public PlayerDialogue hideContinue() {
        this.hideContinue = true;
        return this;
    }

    @Override
    public void open(Player player) {
        // Rev-228 child layout (Stage 11 Round 2): head=2, name=4, continue=5, message=6 — mirror of
        // NPCDialogue's fix (Alter chatPlayer() uses the same children on 217; PROTOCOL_DUMP.txt
        // shows the client natively sending ResumePauseButton on 217:5).
        player.openInterface(InterfaceType.CHATBOX, Interface.PLAYER_DIALOGUE);
        player.getPacketSender().sendPlayerHead(Interface.PLAYER_DIALOGUE, 2);
        player.getPacketSender().animateInterface(Interface.PLAYER_DIALOGUE, 2, animationId);
        player.getPacketSender().sendString(Interface.PLAYER_DIALOGUE, 4, player.getName());
        player.getPacketSender().sendString(Interface.PLAYER_DIALOGUE, 6, message);
        player.getPacketSender().setTextStyle(Interface.PLAYER_DIALOGUE, 6, 1, 1, lineHeight);
        player.getPacketSender().sendAccessMask(Interface.PLAYER_DIALOGUE, 5, -1, -1, 1);
        if(hideContinue)
            player.getPacketSender().setHidden(Interface.PLAYER_DIALOGUE, 5, true);
        else
            player.getPacketSender().sendString(Interface.PLAYER_DIALOGUE, 5, "Click here to continue");
        if(action != null)
            action.run();
    }

    static {
        InterfaceHandler.register(Interface.PLAYER_DIALOGUE, h -> h.actions[5] = (SimpleAction) Player::continueDialogue);
    }

}
