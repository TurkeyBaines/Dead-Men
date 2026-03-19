package io.dm.deadman.tournament.team;

import io.dm.model.World;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.MessageDialogue;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.utils.Option;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

public class Groups {

    HashMap<String, Group> teamList;

    public Groups() {
        teamList = new HashMap<>();
    }

    public void newTeam(Player p, String name) {
        String ID = getNextID();
        teamList.put(
                ID,
                new Group(
                        ID,
                        name,
                        p.getName()
                )
        );
        p.groupID = ID;
        System.out.println("Created new Team (" + ID + ", " + p.getName() + ", " + name + ")");
    }

    public Group getGroup(Player p, String ID) {
        if (!teamList.containsKey(ID)) {
            p.groupID = null;
            return null;
        }

        return teamList.get(ID);
    }
    public Group getGroup(String ID) {
        if (!teamList.containsKey(ID)) {
            return null;
        }

        return teamList.get(ID);
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom(String.valueOf(System.currentTimeMillis()).getBytes());

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    private String getNextID() {
        return generateRandomString(5);
    }

    public void Invite(Player p1, Player p2) {
        if (p1.groupID == null) {
            p1.sendMessage("You need to be in a Group to invite people");
            return;
        }
        Group group = getGroup(p1, p1.groupID);

        if (p1.groupID == p2.groupID) {
            p1.sendMessage("That person is already in your Group");
            return;
        }

        if (!group.isOwner(p1)) {
            p1.sendMessage("You need to be the Group Owner to invite people");
            return;
        }

        if (group.isFull()) {
            p1.sendMessage("Your team is full");
            return;
        }

        p2.dialogue(new OptionsDialogue(
                p1.getName() + " has invited you to join " + group.name,
                new Option("Accept", () -> group.join(p2)),
                new Option("Decline", () -> p1.sendMessage(p2.getName() + " has declined your invite"))
        ));
    }

    private boolean skip = false;
    private boolean approved = false;

    public void Request(Player p, String ID) {
        if (p.groupID != null) {
            p.sendMessage("You're already in a group!");
            return;
        }

        if (!teamList.containsKey(ID)) {
            p.sendMessage("That group doesn't exist!");
            return;
        }
        Group group = getGroup(ID);

        if (group == null) {
            p.sendMessage("Error retrieving team!");
            return;
        }

        if (group.isFull()) {
            p.sendMessage("That group is full!");
            return;
        }


        Player p2 = World.getPlayer(group.owner);
        if (p2 == null) {
            p.sendMessage("The owner is offline!");
            return;
        }
        skip = false;
        approved = false;

        p.startEvent(e -> {
            int ticks = 0;
            p.lock();
            String dots = ".";
            while (!approved && !skip) {
                p.dialogue(new MessageDialogue("Requesting" + dots));
                if (dots.length() == 1) dots = "..";
                if (dots.length() == 2) dots = "...";
                if (dots.length() == 3) dots = ".";

                ticks++;
                e.delay(1);
                if (ticks > 50) {
                    skip = true;
                }
            }

            if (approved) {
                p.dialogue(
                        new MessageDialogue("Success, " + p2.getName() + " has accepted your request!")
                );
            } else if (!skip) {
                p.dialogue(
                        new MessageDialogue("The request timed out")
                );
            } else {
                p.dialogue(
                        new MessageDialogue("Your request has been denied")
                );
            }
            p.unlock();
        });

        p2.dialogue(new OptionsDialogue(
                p.getName() + " would like to join your group",
                new Option("Allow", () -> { group.join(p); approved = true; }),
                new Option("Deny", this::setSkipTrue)
        ));
    }

    private void setSkipTrue() {
        skip = true;
    }

}
