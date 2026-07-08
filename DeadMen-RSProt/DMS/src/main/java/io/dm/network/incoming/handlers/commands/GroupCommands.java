package io.dm.network.incoming.handlers.commands;

import io.dm.deadman.Deadman;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.MessageDialogue;

public class GroupCommands {

    public void process(Player p, String... args) {
        switch (args[0]) {
            case "id":
                id(p);
                break;
        }
    }

    private void id(Player p) {
        if (p.groupID == null || Deadman.getGroups().getGroup(p.groupID) == null) {
            p.sendMessage("Error finding group!");
            return;
        }

        p.sendMessage("Your Group ID is: " + p.groupID);
    }


}
