package io.dm.model.inter.dialogue;

import io.dm.model.entity.player.Player;
import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceHandler;
import io.dm.model.inter.InterfaceType;
import io.dm.model.inter.actions.SimpleAction;

public class MessageDialogue implements Dialogue {

    private String message;

    private Runnable action;

    private int lineHeight;

    private boolean hideContinue;

    public MessageDialogue(String message) {
        this.message = message;
    }

    public MessageDialogue action(Runnable action) {
        this.action = action;
        return this;
    }

    public MessageDialogue lineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }

    public MessageDialogue hideContinue() {
        this.hideContinue = true;
        return this;
    }

    @Override
    public void open(Player player) {
        player.openInterface(InterfaceType.CHATBOX, Interface.MESSAGE_DIALOGUE);
        player.getPacketSender().sendString(Interface.MESSAGE_DIALOGUE, 1, message);
        player.getPacketSender().setTextStyle(Interface.MESSAGE_DIALOGUE, 1, 1, 1, lineHeight);
        // Stage 11 Round 2: 229's children (message=1, continue=2) already match rev 228 (Alter
        // messageBox()), but the continue needs its pause-events mask or the 228 client never sends
        // the ResumePauseButton for it.
        player.getPacketSender().sendAccessMask(Interface.MESSAGE_DIALOGUE, 2, -1, -1, 1);
        if(hideContinue)
            player.getPacketSender().setHidden(Interface.MESSAGE_DIALOGUE, 2, true);
        else
            player.getPacketSender().sendString(Interface.MESSAGE_DIALOGUE, 2, "Click here to continue");
        if(action != null)
            action.run();
    }

    static {
        InterfaceHandler.register(Interface.MESSAGE_DIALOGUE, h -> h.actions[2] = (SimpleAction) Player::continueDialogue);
    }

}