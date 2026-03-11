package io.dm.deadman.areas.overworld;

import io.dm.cache.ItemID;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.utils.Option;
import io.dm.model.item.Item;
import io.dm.model.item.containers.Inventory;

import java.util.HashMap;

public class Resources {



    private static HashMap<Integer, Integer> resources;

    static {
        resources = new HashMap<>();

        // --- WOODCUTTING & FLETCHING ---
        resources.put(ItemID.BOW_STRING,            40);

// Normal
        resources.put(ItemID.LOGS,                  15);
        resources.put(ItemID.SHORTBOW_U,            20);
        resources.put(ItemID.SHORTBOW,              60);
        resources.put(ItemID.LONGBOW_U,             25);
        resources.put(ItemID.LONGBOW,               65);
// Oak
        resources.put(ItemID.OAK_LOGS,              60);
        resources.put(ItemID.OAK_SHORTBOW_U,        80);
        resources.put(ItemID.OAK_SHORTBOW,          120);
        resources.put(ItemID.OAK_LONGBOW_U,         85);
        resources.put(ItemID.OAK_LONGBOW,           125);
// Willow
        resources.put(ItemID.WILLOW_LOGS,           225);
        resources.put(ItemID.WILLOW_SHORTBOW_U,     260);
        resources.put(ItemID.WILLOW_SHORTBOW,       300);
        resources.put(ItemID.WILLOW_LONGBOW_U,      270);
        resources.put(ItemID.WILLOW_LONGBOW,        310);
// Maple
        resources.put(ItemID.MAPLE_LOGS,            562);
        resources.put(ItemID.MAPLE_SHORTBOW_U,      612);
        resources.put(ItemID.MAPLE_SHORTBOW,        652);
        resources.put(ItemID.MAPLE_LONGBOW_U,       617);
        resources.put(ItemID.MAPLE_LONGBOW,         657);
// Yew
        resources.put(ItemID.YEW_LOGS,              1200);
        resources.put(ItemID.YEW_SHORTBOW_U,        1265);
        resources.put(ItemID.YEW_SHORTBOW,          1305);
        resources.put(ItemID.YEW_LONGBOW_U,         1270);
        resources.put(ItemID.YEW_LONGBOW,           1310);
// Magic
        resources.put(ItemID.MAGIC_LOGS,            2400);
        resources.put(ItemID.MAGIC_SHORTBOW_U,      2480);
        resources.put(ItemID.MAGIC_SHORTBOW,        2520);
        resources.put(ItemID.MAGIC_LONGBOW_U,       2485);
        resources.put(ItemID.MAGIC_LONGBOW,         2525);

// --- MINING & SMITHING ---
// Ores
        resources.put(ItemID.COPPER_ORE,            11);
        resources.put(ItemID.TIN_ORE,               11);
        resources.put(ItemID.IRON_ORE,              45);
        resources.put(ItemID.COAL,                  100);
        resources.put(ItemID.GOLD_ORE,              150);
        resources.put(ItemID.MITHRIL_ORE,           262);
        resources.put(ItemID.ADAMANTITE_ORE,        412);
        resources.put(ItemID.RUNITE_ORE,            562);
// Bronze
        resources.put(ItemID.BRONZE_BAR,            25);
        resources.put(ItemID.BRONZE_DAGGER,         26);
        resources.put(ItemID.BRONZE_AXE,            26);
        resources.put(ItemID.BRONZE_MACE,           27);
        resources.put(ItemID.BRONZE_MED_HELM,       28);
        resources.put(ItemID.BRONZE_BOLTS_UNF,      28);
        resources.put(ItemID.BRONZE_SWORD,          29);
        resources.put(ItemID.BRONZE_DART_TIP,       3);
        resources.put(ItemID.BRONZE_WIRE,           29);
        resources.put(ItemID.BRONZE_NAILS,          2);
        resources.put(ItemID.BRONZE_SCIMITAR,       55);
        resources.put(ItemID.BRONZE_SPEAR,          30);
        resources.put(ItemID.BRONZE_HASTA,          30);
        resources.put(ItemID.BRONZE_ARROWTIPS,      2);
        resources.put(ItemID.BRONZE_LIMBS,          31);
        resources.put(ItemID.BRONZE_LONGSWORD,      56);
        resources.put(ItemID.BRONZE_JAVELIN_HEADS,  31);
        resources.put(ItemID.BRONZE_FULL_HELM,      57);
        resources.put(ItemID.BRONZE_KNIFE,          32);
        resources.put(ItemID.BRONZE_SQ_SHIELD,      57);
        resources.put(ItemID.BRONZE_WARHAMMER,      84);
        resources.put(ItemID.BRONZE_BATTLEAXE,      85);
        resources.put(ItemID.BRONZE_CHAINBODY,      86);
        resources.put(ItemID.BRONZE_KITESHIELD,     87);
        resources.put(ItemID.BRONZE_CLAWS,          62);
        resources.put(ItemID.BRONZE_2H_SWORD,       89);
        resources.put(ItemID.BRONZE_PLATELEGS,      91);
        resources.put(ItemID.BRONZE_PLATESKIRT,     91);
        resources.put(ItemID.BRONZE_PLATEBODY,      143);
// Iron
        resources.put(ItemID.IRON_BAR,              60);
        resources.put(ItemID.IRON_DAGGER,           75);
        resources.put(ItemID.IRON_AXE,              76);
        resources.put(ItemID.IRON_MACE,             77);
        resources.put(ItemID.IRON_MED_HELM,         78);
        resources.put(ItemID.IRON_BOLTS_UNF,        8);
        resources.put(ItemID.IRON_SWORD,            79);
        resources.put(ItemID.IRON_DART_TIP,         8);
        resources.put(ItemID.IRON_NAILS,            6);
        resources.put(ItemID.IRON_SCIMITAR,         140);
        resources.put(ItemID.IRON_SPEAR,            80);
        resources.put(ItemID.IRON_HASTA,            80);
        resources.put(ItemID.IRON_ARROWTIPS,        6);
        resources.put(ItemID.IRON_LIMBS,            83);
        resources.put(ItemID.IRON_LONGSWORD,        141);
        resources.put(ItemID.IRON_JAVELIN_HEADS,    81);
        resources.put(ItemID.IRON_FULL_HELM,        142);
        resources.put(ItemID.IRON_KNIFE,            17);
        resources.put(ItemID.IRON_SQ_SHIELD,        143);
        resources.put(ItemID.IRON_WARHAMMER,        204);
        resources.put(ItemID.IRON_BATTLEAXE,        205);
        resources.put(ItemID.IRON_CHAINBODY,        206);
        resources.put(ItemID.IRON_KITESHIELD,       207);
        resources.put(ItemID.IRON_CLAWS,            148);
        resources.put(ItemID.IRON_2H_SWORD,         209);
        resources.put(ItemID.IRON_PLATELEGS,        211);
        resources.put(ItemID.IRON_PLATESKIRT,       211);
        resources.put(ItemID.IRON_PLATEBODY,        333);
// Steel
        resources.put(ItemID.STEEL_BAR,             275);
        resources.put(ItemID.STEEL_DAGGER,          305);
        resources.put(ItemID.STEEL_AXE,             306);
        resources.put(ItemID.STEEL_MACE,            307);
        resources.put(ItemID.STEEL_MED_HELM,        308);
        resources.put(ItemID.STEEL_BOLTS_UNF,       31);
        resources.put(ItemID.STEEL_SWORD,           309);
        resources.put(ItemID.STEEL_DART_TIP,        31);
        resources.put(ItemID.STEEL_NAILS,           21);
        resources.put(ItemID.STEEL_SCIMITAR,        585);
        resources.put(ItemID.STEEL_SPEAR,           310);
        resources.put(ItemID.STEEL_HASTA,           310);
        resources.put(ItemID.STEEL_ARROWTIPS,       21);
        resources.put(ItemID.STEEL_LIMBS,           311);
        resources.put(ItemID.STEEL_LONGSWORD,       586);
        resources.put(ItemID.STEEL_JAVELIN_HEADS,   62);
        resources.put(ItemID.STEEL_FULL_HELM,       312);
        resources.put(ItemID.STEEL_KNIFE,           62);
        resources.put(ItemID.STEEL_SQ_SHIELD,       588);
        resources.put(ItemID.STEEL_WARHAMMER,       864);
        resources.put(ItemID.STEEL_BATTLEAXE,       865);
        resources.put(ItemID.STEEL_CHAINBODY,       866);
        resources.put(ItemID.STEEL_KITESHIELD,      867);
        resources.put(ItemID.STEEL_CLAWS,           593);
        resources.put(ItemID.STEEL_2H_SWORD,        869);
        resources.put(ItemID.STEEL_PLATELEGS,       871);
        resources.put(ItemID.STEEL_PLATESKIRT,      871);
        resources.put(ItemID.STEEL_PLATEBODY,       1423);
// Gold
        resources.put(ItemID.GOLD_BAR,              217);
// Mithril
        resources.put(ItemID.MITHRIL_BAR,           712);
        resources.put(ItemID.MITHRIL_DAGGER,        762);
        resources.put(ItemID.MITHRIL_AXE,           763);
        resources.put(ItemID.MITHRIL_MACE,          764);
        resources.put(ItemID.MITHRIL_MED_HELM,      765);
        resources.put(ItemID.MITHRIL_BOLTS_UNF,     76);
        resources.put(ItemID.MITHRIL_SWORD,         766);
        resources.put(ItemID.MITHRIL_DART_TIP,      76);
        resources.put(ItemID.MITHRIL_NAILS,         51);
        resources.put(ItemID.MITHRIL_SCIMITAR,      1479);
        resources.put(ItemID.MITHRIL_SPEAR,         767);
        resources.put(ItemID.MITHRIL_HASTA,         767);
        resources.put(ItemID.MITHRIL_ARROWTIPS,     51);
        resources.put(ItemID.MITHRIL_LIMBS,         768);
        resources.put(ItemID.MITHRIL_LONGSWORD,     1480);
        resources.put(ItemID.MITHRIL_JAVELIN_HEADS, 153);
        resources.put(ItemID.MITHRIL_FULL_HELM,     1481);
        resources.put(ItemID.MITHRIL_KNIFE,         154);
        resources.put(ItemID.MITHRIL_SQ_SHIELD,     588);
        resources.put(ItemID.MITHRIL_WARHAMMER,     864);
        resources.put(ItemID.MITHRIL_BATTLEAXE,     865);
        resources.put(ItemID.MITHRIL_CHAINBODY,     866);
        resources.put(ItemID.MITHRIL_KITESHIELD,    867);
        resources.put(ItemID.MITHRIL_CLAWS,         593);
        resources.put(ItemID.MITHRIL_2H_SWORD,      869);
        resources.put(ItemID.MITHRIL_PLATELEGS,     871);
        resources.put(ItemID.MITHRIL_PLATESKIRT,    871);
        resources.put(ItemID.MITHRIL_PLATEBODY,     1423);
// Adamant
        resources.put(ItemID.ADAMANTITE_BAR,        1062);
        resources.put(ItemID.ADAMANT_DAGGER,        1132);
        resources.put(ItemID.ADAMANT_AXE,           1133);
        resources.put(ItemID.ADAMANT_MACE,          1134);
        resources.put(ItemID.ADAMANT_MED_HELM,      1135);
        resources.put(ItemID.ADAMANT_BOLTS_UNF,     114);
        resources.put(ItemID.ADAMANT_SWORD,         1136);
        resources.put(ItemID.ADAMANT_DART_TIP,      114);
        resources.put(ItemID.ADAMANTITE_NAILS,      76);
        resources.put(ItemID.ADAMANT_SCIMITAR,      2199);
        resources.put(ItemID.ADAMANT_SPEAR,         1137);
        resources.put(ItemID.ADAMANT_HASTA,         1137);
        resources.put(ItemID.ADAMANT_ARROWTIPS,     76);
        resources.put(ItemID.ADAMANTITE_LIMBS,      1138);
        resources.put(ItemID.ADAMANT_LONGSWORD,     2200);
        resources.put(ItemID.ADAMANT_JAVELIN_HEADS, 228);
        resources.put(ItemID.ADAMANT_FULL_HELM,     2201);
        resources.put(ItemID.ADAMANT_KNIFE,         228);
        resources.put(ItemID.ADAMANT_SQ_SHIELD,     2202);
        resources.put(ItemID.ADAMANT_WARHAMMER,     3265);
        resources.put(ItemID.ADAMANT_BATTLEAXE,     3266);
        resources.put(ItemID.ADAMANT_CHAINBODY,     3267);
        resources.put(ItemID.ADAMANT_KITESHIELD,    3268);
        resources.put(ItemID.ADAMANT_CLAWS,         2207);
        resources.put(ItemID.ADAMANT_2H_SWORD,      2270);
        resources.put(ItemID.ADAMANT_PLATELEGS,     2272);
        resources.put(ItemID.ADAMANT_PLATESKIRT,    2272);
        resources.put(ItemID.ADAMANT_PLATEBODY,     5398);
// Runite
        resources.put(ItemID.RUNITE_BAR,            1447);
        resources.put(ItemID.RUNE_DAGGER,           1532);
        resources.put(ItemID.RUNE_AXE,              1533);
        resources.put(ItemID.RUNE_MACE,             1534);
        resources.put(ItemID.RUNE_MED_HELM,         1535);
        resources.put(ItemID.RUNITE_BOLTS_UNF,      154);
        resources.put(ItemID.RUNE_SWORD,            1536);
        resources.put(ItemID.RUNE_DART_TIP,         154);
        resources.put(ItemID.RUNE_NAILS,            102);
        resources.put(ItemID.RUNE_SCIMITAR,         2984);
        resources.put(ItemID.RUNE_SPEAR,            1537);
        resources.put(ItemID.RUNE_HASTA,            1537);
        resources.put(ItemID.RUNE_ARROWTIPS,        103);
        resources.put(ItemID.RUNITE_LIMBS,          1538);
        resources.put(ItemID.RUNE_LONGSWORD,        2985);
        resources.put(ItemID.RUNE_JAVELIN_HEADS,    307);
        resources.put(ItemID.RUNE_FULL_HELM,        2986);
        resources.put(ItemID.RUNE_KNIFE,            308);
        resources.put(ItemID.RUNE_SQ_SHIELD,        2987);
        resources.put(ItemID.RUNE_WARHAMMER,        4435);
        resources.put(ItemID.RUNE_BATTLEAXE,        4436);
        resources.put(ItemID.RUNE_CHAINBODY,        4437);
        resources.put(ItemID.RUNE_KITESHIELD,       4438);
        resources.put(ItemID.RUNE_CLAWS,            2992);
        resources.put(ItemID.RUNE_2H_SWORD,         4439);
        resources.put(ItemID.RUNE_PLATELEGS,        4439);
        resources.put(ItemID.RUNE_PLATESKIRT,       4439);
        resources.put(ItemID.RUNE_PLATEBODY,        7334);

// --- RUNECRAFTING ---
// Essence
        resources.put(ItemID.RUNE_ESSENCE,          4);
        resources.put(ItemID.PURE_ESSENCE,          12);

// Elementals (Below Pure Essence)
        resources.put(ItemID.AIR_RUNE,              6);
        resources.put(ItemID.MIND_RUNE,             7);
        resources.put(ItemID.WATER_RUNE,            8);
        resources.put(ItemID.EARTH_RUNE,            9);
        resources.put(ItemID.FIRE_RUNE,             10);

// Specialized & High Level (Above Pure Essence)
        resources.put(ItemID.BODY_RUNE,             15);
        resources.put(ItemID.COSMIC_RUNE,           35);
        resources.put(ItemID.CHAOS_RUNE,            55);
        resources.put(ItemID.ASTRAL_RUNE,           75);
        resources.put(ItemID.NATURE_RUNE,           110);
        resources.put(ItemID.LAW_RUNE,              150);
        resources.put(ItemID.DEATH_RUNE,            220);
        resources.put(ItemID.BLOOD_RUNE,            450);
        resources.put(ItemID.SOUL_RUNE,             750);
        resources.put(ItemID.WRATH_RUNE,            1200);

// --- FISHING & COOKING ---
// Small Net & Bait (Shrimp, Anchovies, Sardine, Herring)
        resources.put(ItemID.RAW_SHRIMPS,            10);
        resources.put(ItemID.SHRIMPS,                15);
        resources.put(ItemID.RAW_ANCHOVIES,          15);
        resources.put(ItemID.ANCHOVIES,              22);
        resources.put(ItemID.RAW_SARDINE,            18);
        resources.put(ItemID.SARDINE,                25);
        resources.put(ItemID.RAW_HERRING,            22);
        resources.put(ItemID.HERRING,                32);

// Fly Fishing & Bait (Trout, Salmon, Pike)
        resources.put(ItemID.RAW_TROUT,              45);
        resources.put(ItemID.TROUT,                  65);
        resources.put(ItemID.RAW_PIKE,               55);
        resources.put(ItemID.PIKE,                   80);
        resources.put(ItemID.RAW_SALMON,             75);
        resources.put(ItemID.SALMON,                 110);

// Cage & Harpoon (Tuna, Lobster, Swordfish)
        resources.put(ItemID.RAW_TUNA,               120);
        resources.put(ItemID.TUNA,                   170);
        resources.put(ItemID.RAW_LOBSTER,            280);
        resources.put(ItemID.LOBSTER,                380);
        resources.put(ItemID.RAW_SWORDFISH,          350);
        resources.put(ItemID.SWORDFISH,              490);

// Specialized (Monkfish, Shark, Minnows)
        resources.put(ItemID.RAW_MONKFISH,           520);
        resources.put(ItemID.MONKFISH,               710);
        resources.put(ItemID.RAW_SHARK,              850);
        resources.put(ItemID.SHARK,                  1150);
        resources.put(ItemID.MINNOW,                 5); // Stackable/High Volume

    }

    public static int get(int ID) {
        return resources.get(ID);
    }

    public static boolean contains(int ID) {
        return resources.containsKey(ID);
    }

    public static void noteResources(Player p) {
        Inventory inv = p.getInventory();
        for (int index = 0; index < 28; index++) {
            Item i = inv.get(index);
            if (i == null) continue;
            int id = i.getId();
            if (i.getDef().isNote())
                continue;
            if (i.getDef().stackable)
                continue;
            if (!Resources.contains(id))
                continue;

            int noteID = i.getDef().notedId;
            if (noteID == i.getId()) continue;

            p.getInventory().remove(i);
            p.getInventory().add(noteID, 1);
        }
        p.getInventory().sendUpdates();
    }

    public static void useItem(Player p, Item i) {
        if (!i.getDef().isNote() || !i.getDef().stackable) return;
        if (!resources.containsKey(i.getDef().fromNote().id)) return;

        if (i.getDef().isNote()) {

            p.dialogue(
                    new OptionsDialogue(
                            i.getDef().name,
                            new Option("Unnote", () -> {
                                int quantity = p.getInventory().getFreeSlots();
                                if (i.getDef().fromNote().stackable) quantity = i.getAmount();
                                if (i.getAmount() < quantity) quantity = i.getAmount();

                                p.getInventory().remove(i.getId(), quantity);
                                p.getInventory().add(i.getDef().fromNote().id, quantity);
                            }),
                            new Option("Sell  (" + calculatePoints(i) + " Points)", () -> {
                                int points = calculatePoints(i);
                                p.getInventory().remove(i);
                                p.getInventory().sendUpdates();
                                p.overworldPoints += points;
                                p.getPacketSender().sendMessage("You deposit resources worth " + points + " Points, giving you a new total of " + p.overworldPoints + " Overworld Points!", "", 0);
                            })
                    )
            );

        } else {

            p.dialogue(
                    new OptionsDialogue(
                            i.getDef().name,
                            new Option("Sell  (" + calculatePoints(i) + " Points)", () -> {
                                int points = calculatePoints(i);
                                p.getInventory().remove(i);
                                p.getInventory().sendUpdates();
                                p.overworldPoints += points;
                                p.getPacketSender().sendMessage("You deposit resources worth " + points + " Points, giving you a new total of " + p.overworldPoints + " Overworld Points!", "", 0);
                            })
                    )
            );

        }


    }

    public static void removeResources(Player p) {
        int totalPoints = 0;

        for (Item item : p.getInventory().getItems()) {
            if (item == null) continue;

            if (item.getDef().isNote()) {
                continue;
            }

            if (item.getDef().stackable) {
                continue;
            }

            if (resources.containsKey(item.getId())) {
                totalPoints += resources.get(item.getId());
                p.getInventory().remove(item);
            }
        }
        p.getInventory().sendUpdates();

        p.overworldPoints += totalPoints;

        if (totalPoints == 0) {
            p.getPacketSender().sendMessage("You don't have anything to deposit.", "", 0);
            return;
        }
        p.getPacketSender().sendMessage("You deposit resources worth " + totalPoints + " Points, giving you a new total of " + p.overworldPoints + " Overworld Points!", "", 0);
    }

    public static int calculatePoints(Player p) {
        int totalPoints = 0;

        for (Item item : p.getInventory().getItems()) {
            if (item == null) continue;

            if (item.getDef().isNote()) {
                int id = item.getDef().fromNote().id;
                if (!resources.containsKey(id)) continue;

                int amount = item.getAmount();
                int points = resources.get(id);
                totalPoints += (points * amount);
                continue;
            }

            if (resources.containsKey(item.getId())) {
                totalPoints += resources.get(item.getId());
            }
        }
        return totalPoints;
    }

    public static int calculatePoints(Item item) {
        int totalPoints = 0;

        if (item == null) return -1;

        if (item.getDef().isNote()) {
            int id = item.getDef().fromNote().id;
            if (!resources.containsKey(id)) return -1;

            int amount = item.getAmount();
            int points = resources.get(id);
            totalPoints += (points * amount);
        } else if (item.getDef().stackable) {
            int id = item.getId();
            if (!resources.containsKey(id)) return -1;

            int amount = item.getAmount();
            int points = resources.get(id);
            totalPoints += (points * amount);
        } else {
            int id = item.getId();
            if (!resources.containsKey(id)) return -1;

            totalPoints += resources.get(id);
        }

        return totalPoints;
    }
}
