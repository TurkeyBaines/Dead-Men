package io.dm.model.inter.dialogue;

import io.dm.model.entity.player.Player;
import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceHandler;
import io.dm.model.inter.InterfaceType;
import io.dm.model.inter.actions.SlotAction;
import io.dm.model.item.Item;

public class YesNoDialogue implements Dialogue {

    private int itemId, itemAmount;

    private String title, message;

    private String customItemName;

    private Runnable yesAction;

    public YesNoDialogue(String title, String message, Item item, Runnable yesAction) {
        this(title, message, item.getId(), item.getAmount(), null, yesAction);
    }

    public YesNoDialogue(String title, String message, Item item, String customItemName, Runnable yesAction) {
        this(title, message, item.getId(), item.getAmount(), customItemName, yesAction);
    }

    public YesNoDialogue(String title, String message, int itemId, int itemAmount, Runnable yesAction) {
        this(title, message, itemId, itemAmount, null, yesAction);
    }

    public YesNoDialogue(String title, String message, int itemId, int itemAmount, String customItemName, Runnable yesAction) {
        this.title = title;
        this.message = message;
        this.itemId = itemId;
        this.itemAmount = itemAmount;
        this.customItemName = customItemName;
        this.yesAction = yesAction;
    }

    @Override
    public void open(Player player) {
        player.yesNoDialogue = this;
        player.openInterface(InterfaceType.CHATBOX, Interface.YES_NO_DIALOGUE);
        // Stage 11 Round 2: rev 228's confirmdestroy_init (script 814) grew a third int arg between
        // the old deob (obj, int, string, string) and 228 — AlterRSPS destroyItem() passes
        // (item, amount, 0, title, note). Sending the old 4-arg form desyncs the string reads.
        player.getPacketSender().sendClientScript(814, "oiiss", itemId, itemAmount, 0, title, message);
        if(customItemName != null)
            player.getPacketSender().sendString(Interface.YES_NO_DIALOGUE, 6, customItemName);
        player.getPacketSender().sendAccessMask(584, 0, 0, 1, 1);
    }

    static {
        InterfaceHandler.register(Interface.YES_NO_DIALOGUE, h -> h.actions[0] = (SlotAction) (p, slot) -> {
            if(slot == 1) {
                p.yesNoDialogue.yesAction.run();
                if(p.yesNoDialogue == null || p.yesNoDialogue != p.lastDialogue)
                    return;
            }
            p.closeDialogue();
        });
    }

}