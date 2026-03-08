package io.dm.deadman.areas.overworld;

import io.dm.cache.ItemID;
import io.dm.model.World;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.npc.NPCAction;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.player.PlayerAction;
import io.dm.model.inter.dialogue.MessageDialogue;
import io.dm.model.inter.dialogue.NPCDialogue;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.utils.Config;
import io.dm.model.inter.utils.Option;
import io.dm.model.item.actions.ItemObjectAction;
import io.dm.model.map.Bounds;
import io.dm.model.map.MapListener;
import io.dm.model.map.Position;
import io.dm.model.map.object.GameObject;
import io.dm.model.map.object.actions.ObjectAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Overworld {

    private static List<Player> players;
    private HashMap<Integer, Integer> resources;

    private static Bounds bounds = new Bounds(2481,3812,2630,3927, 0);

    public Overworld() {
        players = new ArrayList<>();

        ObjectAction.register(26674, 1, ((player, obj) -> {
            player.startEvent(e -> Resources.removeResources(player));
        }));

        ObjectAction.register(26674, 2, (player, obj) -> Resources.noteResources(player));

        ObjectAction.register(26674, 3, ((player, obj) -> {
            player.startEvent(e -> player.sendMessage("Your inventory has a value of " + Resources.calculatePoints(player) + " Overworld Points!"));
        }));

        ItemObjectAction.register(26674, (player, item, obj) -> Resources.useItem(player, item));

        ObjectAction.register(15127, "Take-axe", (player, obj) -> givePickaxe(player));

        ObjectAction.register(5581, "Take-axe", (player, obj) -> giveAxe(player));

        ObjectAction.register(30971, "Take-tool", (player, obj) -> giveFishingTool(player));

        NPCAction.register(3246, 1, ((p, n) -> {
            n.startEvent(e -> {
                sendAbout(p, n, -2);
            });
        }));

        OverworldTools.register();
        MapListener.registerBounds(bounds)
                .onEnter(Overworld::entered)
                .onExit(Overworld::exited);
    }

    private static void entered(Player player) {
        players.add(player);
        player.setAction(1, null);
        player.attackPlayerListener = Overworld::allowAttack;
        Config.IN_PVP_AREA.set(player, 0);

    }

    private static void exited(Player player, boolean logout) {
        players.remove(player);
        if (logout) return;
        player.setAction(1, PlayerAction.ATTACK);
        Config.IN_PVP_AREA.set(player, 1);
    }

    private void sendAbout(Player p, NPC n, int option) {
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

    private static boolean allowAttack(Player player, Player pTarget, boolean message) {
        return false;
    }

    public boolean contains(Player player) {
        return bounds.inBounds(player.getPosition());
    }

    private void giveFishingTool(Player player) {
        if (!player.getInventory().contains(ItemID.OVERWORLD_FISHING_TOOLS))
            player.getInventory().addOrDrop(ItemID.OVERWORLD_FISHING_TOOLS, 1, null);
        else
            player.dialogue(new MessageDialogue("You already have fishing tools to use."));
    }

    private void giveAxe(Player player) {
        if (!player.getInventory().contains(ItemID.OVERWORLD_AXE))
            player.getInventory().addOrDrop(ItemID.OVERWORLD_AXE, 1, null);
        else
            player.dialogue(new MessageDialogue("You already have a pickaxe to use."));
    }

    private void givePickaxe(Player player) {
        if (!player.getInventory().contains(ItemID.OVERWORLD_PICKAXE))
            player.getInventory().addOrDrop(ItemID.OVERWORLD_PICKAXE, 1, null);
        else
            player.dialogue(new MessageDialogue("You already have a axe to use."));
    }

    private void resetChest(GameObject object) {
        if (object == null) return;
        Position pos = object.getPosition();
        int dir = object.direction;
        object.remove();
        object = new GameObject(13291, pos.getX(), pos.getY(), pos.getZ(), 10, dir);
        object.spawn();
    }

    public int count() {
        return players.size();
    }
}
