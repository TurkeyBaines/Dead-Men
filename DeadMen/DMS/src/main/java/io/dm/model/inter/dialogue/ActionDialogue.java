package io.dm.model.inter.dialogue;

import io.dm.model.entity.player.Player;
import io.dm.model.inter.InterfaceType;

public class ActionDialogue implements Dialogue {

    private Runnable action;

    public ActionDialogue(Runnable action) {
        this.action = action;
    }

    @Override
    public void open(Player player) {
        player.closeInterface(InterfaceType.CHATBOX);
        action.run();
    }

}