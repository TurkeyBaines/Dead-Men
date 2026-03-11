package io.dm.deadman.content.areas.overworld;

import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.npc.NPCAction;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.NPCDialogue;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.utils.Option;

public class OverworldNPCs {

    public static void register() {
        NPCAction.register(3246, 1, ((p, n) -> {
            n.startEvent(e -> {
                sendAbout(p, n, -2);
            });
        }));

    }

    private static void sendAbout(Player p, NPC n, int option) {
        switch (option) {
            case -2:
                p.startEvent(e -> {
                    p.dialogue(
                            new NPCDialogue(n, "Welcome to the Overworld! What can I help you with?")
                    );
                    e.waitForDialogue(p);
                    sendAbout(p, n, -1);
                });
                break;
            case -1:
                p.startEvent(e -> {
                    p.dialogue(
                            new OptionsDialogue(
                                    "What can I help you with?",
                                    new Option("What is this place?", () -> sendAbout(p, n, 0)),
                                    new Option("Where did my stuff go?!?", () -> sendAbout(p, n, 5)),
                                    new Option("How much is this item worth?", () -> sendAbout(p, n, 6))
                            )
                    );
                    e.waitForDialogue(p);
                });
                break;

            case 0:
                p.startEvent(e -> {
                    p.dialogue(
                            new NPCDialogue(n, "This place is called the Overworld, it's detached from the main game!"),
                            new NPCDialogue(n, "Items and Stats work a little differently here, and you can power-up your tournament Character by leveling up your stats here!")
                    );
                    e.waitForDialogue(p);
                    sendAbout(p, n, 1);
                });
                break;

            case 1:
                p.startEvent(e -> {
                    p.dialogue(
                            new OptionsDialogue(
                                    new Option("What are Overworld Points?", () -> sendAbout(p, n, 2)),
                                    new Option("How do i progress here?", () -> sendAbout(p, n, 3)),
                                    new Option("I'm 99, how do I keep going?", () -> sendAbout(p, n, 4))
                            )
                    );
                    e.waitForDialogue(p);
                });
                break;

            case 2: // What are overworld points?
                p.startEvent(e -> {
                    p.dialogue(
                            new NPCDialogue(n, "Overworld Points allow you to purchase cool upgrades for your DMM Character"),
                            new NPCDialogue(n, "Most of these upgrades will carry on between tournaments, some will only last a few games though!"),
                            new NPCDialogue(n, "Visit the shop on the either the east or west of the island to view what is available.")
                    );
                    e.waitForDialogue(p);
                    sendAbout(p, n, 1);
                });
                break;

            case 3: // How do i progress here?
                p.startEvent(e -> {
                    p.dialogue(
                            new NPCDialogue(n, "The aim of the Overworld is to train your stats and use the Hoppers placed around the area to deposit your resources."),
                            new NPCDialogue(n, "The better the resource, the more points it is worth to the Hopper. You can also process resources into completed items to earn bonus points!"),
                            new NPCDialogue(n, "You can then spend your hard earned points on some cool rewards, and even Prestige your skills for a nice boost above 99!"),
                            new OptionsDialogue("Would you like to learn more?", new Option("Yes", () -> sendAbout(p, n, -1)), new Option("No"))
                    );
                    e.waitForDialogue(p);
                    sendAbout(p, n, 1);
                });
                break;

            case 4: // I'm 99, how do I keep going?
                p.startEvent(e -> {
                    p.dialogue(
                            new NPCDialogue(n, "Once you hit 99 in a skill, you can trade all your levels for prestige"),
                            new NPCDialogue(n, "Prestige is a way to have your levels scale above 99 in tournaments"),
                            new NPCDialogue(n, "You will unlock an additional level for every Prestige, up to a max of 10"),
                            new NPCDialogue(n, "Click the Skill in your Skill Tab once you hit 99 to Prestige the Skill!")
                    );
                    e.waitForDialogue(p);
                    sendAbout(p, n, 1);
                });
                break;

            case 5:
                p.startEvent(e -> {
                    p.dialogue(
                            new NPCDialogue(n, "Your Items, Equipment, and Stats have all been saved and will be returned"),
                            new NPCDialogue(n, "once back at the Citadel. Your Overworld Character is progressed differently"),
                            new NPCDialogue(n, "to the main game tournament, meaning all Stats, Items, and Equipment is stored"),
                            new NPCDialogue(n, "differently. To get everything back, simply step back through the portal!")
                    );
                    e.waitForDialogue(p);
                    sendAbout(p, n, -1);
                });
                break;

            case 6:
                p.startEvent(e -> {
                    p.dialogue(
                            new NPCDialogue(n, "Use an applicable item on me to get a valuation in Overworld Points")
                    );
                    e.waitForDialogue(p);
                    sendAbout(p, n, -1);
                });
                break;
        }
    }
}
