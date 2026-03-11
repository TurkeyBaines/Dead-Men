package io.dm.deadman.areas.overworld;

import com.google.common.collect.Lists;
import io.dm.api.utils.Random;
import io.dm.cache.ItemID;
import io.dm.cache.ObjectID;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.shared.LockType;
import io.dm.model.inter.dialogue.MessageDialogue;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.item.Item;
import io.dm.model.item.actions.ItemObjectAction;
import io.dm.model.item.loot.LootItem;
import io.dm.model.item.loot.LootTable;
import io.dm.model.map.Direction;
import io.dm.model.map.Position;
import io.dm.model.map.object.GameObject;
import io.dm.model.map.object.actions.ObjectAction;
import io.dm.model.skills.herblore.Herb;
import io.dm.model.skills.runecrafting.Altars;
import io.dm.model.skills.runecrafting.Essence;
import io.dm.model.stat.StatType;
import io.dm.utility.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class OverworldObjects {

    public static void register() {

        /*
            Overworld Hopper
         */
        ObjectAction.register(51003, 1, ((player, obj) -> {
            player.startEvent(e -> Resources.removeResources(player));
        }));
        ObjectAction.register(51003, 2, (player, obj) -> Resources.noteResources(player));
        ObjectAction.register(51003, 3, ((player, obj) -> {
            player.startEvent(e -> player.sendMessage("Your inventory has a value of " + Resources.calculatePoints(player) + " Overworld Points!"));
        }));
        ItemObjectAction.register(26674, (player, item, obj) -> Resources.useItem(player, item));

        /*
            Pickaxe Stuck In Rock
         */
        ObjectAction.register(15127, "Take-pickaxe", (player, obj) -> givePickaxe(player));

        /*
            Axe Stuck In Tree Stump
         */
        ObjectAction.register(5581, "Take-axe", (player, obj) -> giveAxe(player));

        /*
            Fishing Chest With Gremlin Inside
         */
        ObjectAction.register(30971, "Take-tool", (player, obj) -> giveFishingTool(player));

        /*
            Rune Ess Pickaxe
         */
        new GameObject(15127, 2550, 3856, 0, 10, 0).spawn();

        /*
            ZMI Altar
         */
        GameObject altar = new GameObject(29631, new Position(2517, 3883, 0), 10, 0);
        int[][] glows = {
                {2516, 3886}, {2520, 3886}, // <-- these are organised if you're standing south of altar looking north!
                {2516, 3882}, {2520, 3882}};
        int[][] pillars = {
                {2518, 3880},
                {2515, 3884},
                {2518, 3887},
                {2522, 3883}
        };
        GameObject glow;
        GameObject pillar;
        for (int i = 0; i < glows.length; i++) {
            glow = new GameObject(34794, new Position(glows[i][0], glows[i][1], 0), 10, 0);

            glow.spawn();
        }
        for (int i = 0; i < pillars.length; i++) {
            if (i == 3)
                pillar = new GameObject(34781, new Position(pillars[i][0], pillars[i][1], 0), 10, 5);
            else
                pillar = new GameObject(34780, new Position(pillars[i][0], pillars[i][1], 0), 10, i);
            pillar.spawn();
        }
        altar.spawn();

//        ObjectAction.register(altar, "Craft-rune", (p, o) -> p.startEvent(event -> {
//
//            List<Altars> altars = Lists.newArrayList(Altars.values());
//            altars.removeIf(a -> a.levelRequirement > p.getStats().get(StatType.Runecrafting).currentLevel);
//
//            int essenceCount = 0, fromPouches = 0;
//            ArrayList<Item> essence = p.getInventory().collectItems(Essence.PURE.id);
//            essenceCount += fromPouches = Altars.essenceFromPouches(p);
//
//            if (essence != null) {
//                essenceCount += essence.size();
//            }
//
//            if (fromPouches > 0) {
//                Altars.clearPouches(p);
//            }
//
//            if (essenceCount > 0) {
//
//                p.lock(LockType.FULL_DELAY_DAMAGE);
//                p.animate(791);
//                p.graphics(186, 100, 0);
//                event.delay(4);
//
//                IntStream.range(0, essenceCount).forEach(i -> {
//                    Altars selectedAltar = Utils.randomTypeOfList(altars);
//                    p.getInventory().add(selectedAltar.runeID, 1);
//                    p.getInventory().remove(Essence.PURE.id, 1);
//                    p.getStats().addXp(StatType.Runecrafting, selectedAltar.experience, true);
//                    selectedAltar.counter.increment(p, 1);
//                });
//
//                p.unlock();
//
//            } else {
//                p.sendMessage("You don't have any essence to craft.");
//            }
//
//        }));


        /*
            Herblore Patch
         */
        ObjectAction.register(35834, "Pick", (p, o) -> {
            p.startEvent(e -> {
                while(p.getInventory().getFreeSlots() > 0) {
                    p.animate(2282);
                    e.delay(4);
                    handleHerbSpot(p);
                    e.delay(1);
                }
            });
        });

        /*
            Seaweed
         */
        ObjectAction.register(51002, "Pick", (p, o) -> {
            p.startEvent(e -> {
                while(p.getInventory().getFreeSlots() > 0) {
                    p.animate(2282);
                    e.delay(1);
                    p.getInventory().add(new Item(ItemID.SEAWEED, 1));
                    e.delay(2);
                }
            });
        });


        /*
            Task Cave Entrance / Exit
         */
        ObjectAction.register(34499, "Enter", (p, o) -> {
            p.getMovement().teleport(2759, 10064);
            p.face(Direction.EAST);
        });

        ObjectAction.register(5859, "Exit", (p, o) -> {
            p.getMovement().teleport(2615, 3853);
            p.face(Direction.WEST);
        });

        /*
            Sandpit / Bucket Spawn
         */
        ObjectAction.register(33309, "Take-1", (p, o) -> {
            p.getInventory().add(new Item(ItemID.BUCKET, 1));
            giveGlassblowingPipe(p);
        });
        ObjectAction.register(33309, "Take-5", (p, o) -> {
            p.getInventory().add(new Item(ItemID.BUCKET, 5));
            giveGlassblowingPipe(p);
        });
        ObjectAction.register(33309, "Take-all", (p, o) -> {
            p.getInventory().add(new Item(ItemID.BUCKET, p.getInventory().getFreeSlots()));
            giveGlassblowingPipe(p);
        });

        ObjectAction.register(51001, "Fill", (p, o) -> {
            if (!p.getInventory().hasItem(ItemID.BUCKET, 1)) {
                p.dialogue(new MessageDialogue("You don't have any buckets to fill up, if only there were some nearby... oh well!"));
                return;
            }
            p.startEvent(e -> {
                for (Item i : p.getInventory().getItems()) {
                    if (i.getId() == ItemID.BUCKET) {
                        p.face(o);

                        p.animate(895);
                        e.delay(1);
                        p.getInventory().replace(i, new Item(ItemID.BUCKET_OF_SAND, 1));
                        e.delay(2);
                    }
                }
            });
        });

    }

    private static void giveGlassblowingPipe(Player p) {
        if (p.getInventory().hasItem(ItemID.GLASSBLOWING_PIPE, 1))
            return;
        p.getInventory().add(new Item(ItemID.GLASSBLOWING_PIPE, 1));
    }

    private static void handleHerbSpot(Player p) {
        if (Random.get(3) == 1) {

            int level = p.getStats().get(StatType.Farming).currentLevel;
            ArrayList<LootItem> herbs = new ArrayList<>();
            for (Herb h : Herb.values())
                if (level >= h.lvlReq || h == Herb.GUAM_LEAF)
                    herbs.add(new LootItem(h.grimyId, 1, h.lvlReq));

            LootTable lootTable = new LootTable().addTable(1, herbs.toArray(LootItem[]::new));

            Item i = lootTable.rollItem();

            double xp = 0;
            for (Herb h : Herb.values())
                if (h.grimyId == i.getId())
                    xp = h.xp * 4;

            p.getInventory().addOrDrop(i);
            p.getStats().addXp(StatType.Farming, xp, true);
        }
    }


    private static void giveFishingTool(Player player) {
        if (!player.getInventory().contains(ItemID.OVERWORLD_FISHING_TOOLS))
            player.getInventory().addOrDrop(ItemID.OVERWORLD_FISHING_TOOLS, 1, null);
        else
            player.dialogue(new MessageDialogue("You already have fishing tools to use."));
    }

    private static void giveAxe(Player player) {
        if (!player.getInventory().contains(ItemID.OVERWORLD_AXE))
            player.getInventory().addOrDrop(ItemID.OVERWORLD_AXE, 1, null);
        else
            player.dialogue(new MessageDialogue("You already have a pickaxe to use."));
    }

    private static void givePickaxe(Player player) {
        if (!player.getInventory().contains(ItemID.OVERWORLD_PICKAXE))
            player.getInventory().addOrDrop(ItemID.OVERWORLD_PICKAXE, 1, null);
        else
            player.dialogue(new MessageDialogue("You already have a axe to use."));
    }

}
