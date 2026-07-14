package io.dm.model.inter.dialogue;

import io.dm.model.entity.player.Player;
import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceHandler;
import io.dm.model.inter.InterfaceType;
import io.dm.model.inter.actions.SimpleAction;

public class ItemDialogue implements Dialogue {

    private int itemId1, itemId2;

    private String message;

    private int lineHeight;

    private Runnable action;

    private boolean hideContinue;

    public ItemDialogue one(int itemId, String message) {
        this.itemId1 = itemId;
        this.itemId2 = -1;
        this.message = message;
        return this;
    }

    public ItemDialogue two(int itemId1, int itemId2, String message) {
        this.itemId1 = itemId1;
        this.itemId2 = itemId2;
        this.message = message;
        return this;
    }

    public ItemDialogue action(Runnable action) {
        this.action = action;
        return this;
    }

    public ItemDialogue lineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }

    public ItemDialogue hideContinue() {
        this.hideContinue = true;
        return this;
    }

    @Override
    public void open(Player player) {
        if(itemId2 == -1) {
            player.openInterface(InterfaceType.CHATBOX, Interface.ONE_ITEM_DIALOGUE);
            player.getPacketSender().sendItem(Interface.ONE_ITEM_DIALOGUE, 1, itemId1, 10000);
            player.getPacketSender().sendString(Interface.ONE_ITEM_DIALOGUE, 2, message);
            player.getPacketSender().setTextStyle(Interface.ONE_ITEM_DIALOGUE, 2, 1, 1, lineHeight);
            // Stage 11 Round 2: script 2868 (objbox_setbuttons) creates the buttons as dynamic
            // children of 193:0 — they need the pause-events mask on those slots (Alter
            // itemMessageBox(): events on 193:0 slots 0-1) for the 228 client to send the click.
            player.getPacketSender().sendAccessMask(Interface.ONE_ITEM_DIALOGUE, 0, 0, 1, 1);
            if(!hideContinue) {
                player.getPacketSender().sendClientScript(2868, "s", "Click here to continue");
            }
        } else {
            player.openInterface(InterfaceType.CHATBOX, Interface.TWO_ITEM_DIALOGUE);
            player.getPacketSender().sendItem(Interface.TWO_ITEM_DIALOGUE, 1, itemId1, 10000);
            player.getPacketSender().sendItem(Interface.TWO_ITEM_DIALOGUE, 3, itemId2, 10000);
            player.getPacketSender().sendString(Interface.TWO_ITEM_DIALOGUE, 2, message);
            player.getPacketSender().setTextStyle(Interface.TWO_ITEM_DIALOGUE, 2, 1, 1, lineHeight);
            // Stage 11 Round 2: children already match rev 228 (Alter doubleItemMessageBox():
            // item1=1, text=2, item2=3, continue=4) — only the events mask was missing.
            player.getPacketSender().sendAccessMask(Interface.TWO_ITEM_DIALOGUE, 4, -1, -1, 1);
            if(hideContinue)
                player.getPacketSender().setHidden(Interface.TWO_ITEM_DIALOGUE, 4, true);
            else
                player.getPacketSender().sendString(Interface.TWO_ITEM_DIALOGUE, 4, "Click here to continue");
        }
        if(action != null)
            action.run();
    }

    static {
        InterfaceHandler.register(Interface.ONE_ITEM_DIALOGUE, h -> h.actions[0] = (SimpleAction) Player::continueDialogue);
        InterfaceHandler.register(Interface.TWO_ITEM_DIALOGUE, h -> h.actions[4] = (SimpleAction) Player::continueDialogue);
    }

}
