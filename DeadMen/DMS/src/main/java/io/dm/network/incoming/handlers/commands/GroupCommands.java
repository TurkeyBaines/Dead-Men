package io.dm.network.incoming.handlers.commands;

import io.dm.deadman.Deadman;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.MessageDialogue;

public class GroupCommands {

    public void process(Player p, String... args) {
        switch (args[0]) {
            case "create":
                create(p);
                break;

            case "leave":
                leaveTeam(p);
                break;
        }
    }


    private void create(Player p) {
        p.stringInput("Enter a name", name -> {
            if (name.length() > 10) {
                p.dialogue(new MessageDialogue("Name must contain less than 10 characters."));
                return;
            }
            Deadman.getGroups().newTeam(p, name);
        });
    }

    private void leaveTeam(Player p) {
        String id = p.groupID;
        if (id == null) {
            p.sendMessage("You're not in a team!");
            return;
        }
        System.out.println("Command: Leave");
        System.out.println("\t - ID: " + id);

        if (!Deadman.getGroups().getGroup(p, id).leave(p)) {
            p.sendMessage("We couldn't find you in the team!");
        }
    }

}
