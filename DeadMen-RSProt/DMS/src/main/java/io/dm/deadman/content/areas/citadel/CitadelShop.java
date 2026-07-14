package io.dm.deadman.content.areas.citadel;

import io.dm.cache.Color;
import io.dm.cache.Icon;
import io.dm.cache.ItemID;
import io.dm.deadman.Deadman;
import io.dm.deadman.tournament.stages.*;
import io.dm.model.World;
import io.dm.model.combat.*;
import io.dm.model.entity.npc.NPC;
import io.dm.model.map.Direction;
import io.dm.model.shop.*;
import io.dm.process.event.Event;
import java.util.ArrayList;
import java.util.List;

public class CitadelShop {

    private static final int[] SHOPKEEPER_IDS = { 15103, 15108, 15105, 15106 };

    public static NPC shopkeep;

    private static final List<ShopItem> FOUNDATIONS = new ArrayList<>();
    private static void populateFoundations() {
        FOUNDATIONS.add(ShopItem.builder().id(ItemID.AIR_RUNE).placeholderId(ItemID.AIR_RUNE).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(5000).price(15).build());
        FOUNDATIONS.add(ShopItem.builder().id(ItemID.MIND_RUNE).placeholderId(ItemID.MIND_RUNE).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(5000).price(15).build());
        FOUNDATIONS.add(ShopItem.builder().id(ItemID.EARTH_RUNE).placeholderId(ItemID.EARTH_RUNE).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(2500).price(20).build());
        FOUNDATIONS.add(ShopItem.builder().id(ItemID.WATER_RUNE).placeholderId(ItemID.WATER_RUNE).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(2500).price(20).build());
        FOUNDATIONS.add(ShopItem.builder().id(ItemID.FIRE_RUNE).placeholderId(ItemID.FIRE_RUNE).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(2000).price(25).build());
        FOUNDATIONS.add(ShopItem.builder().id(ItemID.LAW_RUNE).placeholderId(ItemID.LAW_RUNE).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(750).price(60).build());

        FOUNDATIONS.add(ShopItem.builder().id(ItemID.TROUT).placeholderId(ItemID.TROUT).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(1000).price(15).build());
        FOUNDATIONS.add(ShopItem.builder().id(ItemID.SALMON).placeholderId(ItemID.SALMON).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(1000).price(25).build());

        FOUNDATIONS.add(ShopItem.builder().id(ItemID.ATTACK_POTION3).placeholderId(ItemID.ATTACK_POTION3).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(250).price(180).build());
        FOUNDATIONS.add(ShopItem.builder().id(ItemID.STRENGTH_POTION3).placeholderId(ItemID.STRENGTH_POTION3).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(250).price(180).build());
        FOUNDATIONS.add(ShopItem.builder().id(ItemID.DEFENCE_POTION3).placeholderId(ItemID.DEFENCE_POTION3).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(250).price(180).build());

        FOUNDATIONS.add(ShopItem.builder().id(ItemID.IRON_ARROW).placeholderId(ItemID.IRON_ARROW).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(2500).price(5).build());
        FOUNDATIONS.add(ShopItem.builder().id(ItemID.STEEL_ARROW).placeholderId(ItemID.STEEL_ARROW).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(2500).price(10).build());

        FOUNDATIONS.add(ShopItem.builder().id(ItemID.PRAYER_POTION3).placeholderId(ItemID.PRAYER_POTION3).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(150).price(270).build());
    }

    private static final List<ShopItem> RUSH = new ArrayList<>();
    private static void populateRush() {
        RUSH.add(ShopItem.builder().id(ItemID.MITHRIL_SCIMITAR).placeholderId(ItemID.MITHRIL_SCIMITAR).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(50).price(450).build());
        RUSH.add(ShopItem.builder().id(ItemID.ADAMANT_SCIMITAR).placeholderId(ItemID.ADAMANT_SCIMITAR).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(35).price(700).build());

        RUSH.add(ShopItem.builder().id(ItemID.ADAMANT_MED_HELM).placeholderId(ItemID.ADAMANT_MED_HELM).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(35).price(300).build());
        RUSH.add(ShopItem.builder().id(ItemID.ADAMANT_CHAINBODY).placeholderId(ItemID.ADAMANT_CHAINBODY).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(35).price(500).build());
        RUSH.add(ShopItem.builder().id(ItemID.ADAMANT_PLATELEGS).placeholderId(ItemID.ADAMANT_PLATELEGS).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(35).price(400).build());
        RUSH.add(ShopItem.builder().id(ItemID.ADAMANT_SQ_SHIELD).placeholderId(ItemID.ADAMANT_SQ_SHIELD).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(35).price(300).build());

        RUSH.add(ShopItem.builder().id(ItemID.WILLOW_SHORTBOW).placeholderId(ItemID.WILLOW_SHORTBOW).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(35).price(500).build());
        RUSH.add(ShopItem.builder().id(ItemID.BLUE_DHIDE_BODY).placeholderId(ItemID.BLUE_DHIDE_BODY).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(35).price(500).build());
        RUSH.add(ShopItem.builder().id(ItemID.BLUE_DHIDE_CHAPS).placeholderId(ItemID.BLUE_DHIDE_CHAPS).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(35).price(400).build());
        RUSH.add(ShopItem.builder().id(ItemID.BLUE_DHIDE_VAMB).placeholderId(ItemID.BLUE_DHIDE_VAMB).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(35).price(300).build());

        RUSH.add(ShopItem.builder().id(ItemID.FIRE_BATTLESTAFF).placeholderId(ItemID.FIRE_BATTLESTAFF).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(35).price(300).build());
        RUSH.add(ShopItem.builder().id(ItemID.MYSTIC_ROBE_TOP).placeholderId(ItemID.MYSTIC_ROBE_TOP).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(35).price(300).build());
        RUSH.add(ShopItem.builder().id(ItemID.MYSTIC_ROBE_BOTTOM).placeholderId(ItemID.MYSTIC_ROBE_BOTTOM).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(35).price(300).build());
        RUSH.add(ShopItem.builder().id(ItemID.MYSTIC_HAT).placeholderId(ItemID.MYSTIC_HAT).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(35).price(300).build());
        RUSH.add(ShopItem.builder().id(ItemID.MYSTIC_BOOTS).placeholderId(ItemID.MYSTIC_BOOTS).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(35).price(300).build());

        RUSH.add(ShopItem.builder().id(ItemID.SUPER_ATTACK3).placeholderId(ItemID.SUPER_ATTACK3).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(150).price(540).build());
        RUSH.add(ShopItem.builder().id(ItemID.SUPER_STRENGTH3).placeholderId(ItemID.SUPER_STRENGTH3).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(150).price(540).build());
        RUSH.add(ShopItem.builder().id(ItemID.SUPER_DEFENCE3).placeholderId(ItemID.SUPER_DEFENCE3).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(150).price(540).build());

        RUSH.add(ShopItem.builder().id(ItemID.RUNE_ARROW).placeholderId(ItemID.RUNE_ARROW).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(500).price(50).build());
        RUSH.add(ShopItem.builder().id(ItemID.RUNITE_BOLTS).placeholderId(ItemID.RUNITE_BOLTS).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(500).price(50).build());

        RUSH.add(ShopItem.builder().id(ItemID.CHAOS_RUNE).placeholderId(ItemID.CHAOS_RUNE).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(1000).price(100).build());
        RUSH.add(ShopItem.builder().id(ItemID.COSMIC_RUNE).placeholderId(ItemID.COSMIC_RUNE).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(1000).price(100).build());
        RUSH.add(ShopItem.builder().id(ItemID.NATURE_RUNE).placeholderId(ItemID.NATURE_RUNE).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(1000).price(130).build());
    }

    private static final List<ShopItem> WAR = new ArrayList<>();
    private static void populateWar() {
        WAR.add(ShopItem.builder().id(ItemID.RUNE_SCIMITAR).placeholderId(ItemID.RUNE_SCIMITAR).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(20).price(1600).build());
        WAR.add(ShopItem.builder().id(ItemID.RUNE_MED_HELM).placeholderId(ItemID.RUNE_MED_HELM).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(25).price(1000).build());
        WAR.add(ShopItem.builder().id(ItemID.RUNE_CHAINBODY).placeholderId(ItemID.RUNE_CHAINBODY).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(25).price(1600).build());
        WAR.add(ShopItem.builder().id(ItemID.RUNE_PLATELEGS).placeholderId(ItemID.RUNE_PLATELEGS).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(25).price(1400).build());
        WAR.add(ShopItem.builder().id(ItemID.RUNE_SQ_SHIELD).placeholderId(ItemID.RUNE_SQ_SHIELD).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(25).price(1000).build());

        WAR.add(ShopItem.builder().id(ItemID.MAPLE_SHORTBOW).placeholderId(ItemID.MAPLE_SHORTBOW).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(25).price(1600).build());
        WAR.add(ShopItem.builder().id(ItemID.BLACK_DHIDE_BODY).placeholderId(ItemID.BLACK_DHIDE_BODY).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(25).price(1600).build());
        WAR.add(ShopItem.builder().id(ItemID.BLACK_DHIDE_CHAPS).placeholderId(ItemID.BLACK_DHIDE_CHAPS).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(25).price(1400).build());
        WAR.add(ShopItem.builder().id(ItemID.BLACK_DHIDE_VAMB).placeholderId(ItemID.BLACK_DHIDE_VAMB).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(25).price(1000).build());

        WAR.add(ShopItem.builder().id(ItemID.MASTER_WAND).placeholderId(ItemID.MASTER_WAND).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(25).price(1600).build());
        WAR.add(ShopItem.builder().id(ItemID.INFINITY_TOP).placeholderId(ItemID.INFINITY_TOP).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(25).price(1600).build());
        WAR.add(ShopItem.builder().id(ItemID.INFINITY_BOTTOMS).placeholderId(ItemID.INFINITY_BOTTOMS).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(25).price(1400).build());
        WAR.add(ShopItem.builder().id(ItemID.INFINITY_BOOTS).placeholderId(ItemID.INFINITY_BOOTS).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(25).price(1000).build());
        WAR.add(ShopItem.builder().id(ItemID.INFINITY_HAT).placeholderId(ItemID.INFINITY_HAT).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(25).price(1000).build());

        WAR.add(ShopItem.builder().id(ItemID.DEATH_RUNE).placeholderId(ItemID.DEATH_RUNE).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(750).price(1000).build());
        WAR.add(ShopItem.builder().id(ItemID.BLOOD_RUNE).placeholderId(ItemID.BLOOD_RUNE).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(750).price(1000).build());
    }

    private static final List<ShopItem> SQUEEZE = new ArrayList<>();
    private static void populateSqueeze() {
        SQUEEZE.add(ShopItem.builder().id(ItemID.DRAGON_SCIMITAR).amount(20).price(5000).build());

        SQUEEZE.add(ShopItem.builder().id(ItemID.DHAROKS_HELM).placeholderId(ItemID.DHAROKS_HELM).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(5).price(7500).build());
        SQUEEZE.add(ShopItem.builder().id(ItemID.DHAROKS_GREATAXE).placeholderId(ItemID.DHAROKS_GREATAXE).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(5).price(7500).build());
        SQUEEZE.add(ShopItem.builder().id(ItemID.DHAROKS_PLATEBODY).placeholderId(ItemID.DHAROKS_PLATEBODY).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(5).price(7500).build());
        SQUEEZE.add(ShopItem.builder().id(ItemID.DHAROKS_PLATELEGS).placeholderId(ItemID.DHAROKS_PLATELEGS).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(5).price(7500).build());

        SQUEEZE.add(ShopItem.builder().id(ItemID.GUTHANS_HELM).placeholderId(ItemID.GUTHANS_HELM).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(5).price(7500).build());
        SQUEEZE.add(ShopItem.builder().id(ItemID.GUTHANS_PLATEBODY).placeholderId(ItemID.GUTHANS_PLATEBODY).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(5).price(7500).build());
        SQUEEZE.add(ShopItem.builder().id(ItemID.GUTHANS_CHAINSKIRT).placeholderId(ItemID.GUTHANS_CHAINSKIRT).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(5).price(7500).build());
        SQUEEZE.add(ShopItem.builder().id(ItemID.GUTHANS_WARSPEAR).placeholderId(ItemID.GUTHANS_WARSPEAR).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(5).price(7500).build());

        SQUEEZE.add(ShopItem.builder().id(ItemID.VERACS_HELM).placeholderId(ItemID.VERACS_HELM).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(5).price(7500).build());
        SQUEEZE.add(ShopItem.builder().id(ItemID.VERACS_BRASSARD).placeholderId(ItemID.VERACS_BRASSARD).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(5).price(7500).build());
        SQUEEZE.add(ShopItem.builder().id(ItemID.VERACS_PLATESKIRT).placeholderId(ItemID.VERACS_PLATESKIRT).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(5).price(7500).build());
        SQUEEZE.add(ShopItem.builder().id(ItemID.VERACS_FLAIL).placeholderId(ItemID.VERACS_FLAIL).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(5).price(7500).build());

        SQUEEZE.add(ShopItem.builder().id(ItemID.AHRIMS_HOOD).placeholderId(ItemID.AHRIMS_HOOD).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(5).price(7500).build());
        SQUEEZE.add(ShopItem.builder().id(ItemID.AHRIMS_ROBETOP).placeholderId(ItemID.AHRIMS_ROBETOP).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(5).price(7500).build());
        SQUEEZE.add(ShopItem.builder().id(ItemID.AHRIMS_ROBESKIRT).placeholderId(ItemID.AHRIMS_ROBESKIRT).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(5).price(7500).build());
        SQUEEZE.add(ShopItem.builder().id(ItemID.AHRIMS_STAFF).placeholderId(ItemID.AHRIMS_STAFF).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(5).price(7500).build());

        SQUEEZE.add(ShopItem.builder().id(ItemID.KARILS_COIF).placeholderId(ItemID.KARILS_COIF).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(5).price(7500).build());
        SQUEEZE.add(ShopItem.builder().id(ItemID.KARILS_LEATHERTOP).placeholderId(ItemID.KARILS_LEATHERTOP).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(5).price(7500).build());
        SQUEEZE.add(ShopItem.builder().id(ItemID.KARILS_LEATHERSKIRT).placeholderId(ItemID.KARILS_LEATHERSKIRT).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(5).price(7500).build());
        SQUEEZE.add(ShopItem.builder().id(ItemID.KARILS_CROSSBOW).placeholderId(ItemID.KARILS_CROSSBOW).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(5).price(7500).build());

        SQUEEZE.add(ShopItem.builder().id(ItemID.OVERLOAD_3).placeholderId(ItemID.OVERLOAD_3).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(50).price(1500).build());

        SQUEEZE.add(ShopItem.builder().id(ItemID.DRAGONSTONE_BOLTS_E).placeholderId(ItemID.DRAGONSTONE_BOLTS_E).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(500).price(1500).build());

        SQUEEZE.add(ShopItem.builder().id(ItemID.WRATH_RUNE).placeholderId(ItemID.WRATH_RUNE).placeHolderRule(PlaceHolderRule.SHOW_ON_EMPTY).amount(500).price(2000).build());
    }


    public static void register() {
        populateFoundations();
        Shop foundationShop = Shop.builder().title("Tier 1 Shop")
                .currency(Currency.COINS).canSellToStore(false)
                .defaultStock(FOUNDATIONS).generatedByBuilder(true)
                .accessibleByIronMan(true).build();

        populateRush();
        Shop rushShop = Shop.builder().title("Tier 2 Shop")
                .currency(Currency.COINS).canSellToStore(false)
                .defaultStock(RUSH).generatedByBuilder(true)
                .accessibleByIronMan(true).build();

        populateWar();
        Shop warShop = Shop.builder().title("Tier 3 Shop")
                .currency(Currency.COINS).canSellToStore(false)
                .defaultStock(WAR).generatedByBuilder(true)
                .accessibleByIronMan(true).build();

        populateSqueeze();
        Shop squeezeShop = Shop.builder().title("Tier 4 Shop")
                .currency(Currency.COINS).canSellToStore(false)
                .defaultStock(SQUEEZE).generatedByBuilder(true)
                .accessibleByIronMan(true).build();

        ShopManager.registerShop(foundationShop);
        ShopManager.registerShop(rushShop);
        ShopManager.registerShop(warShop);
        ShopManager.registerShop(squeezeShop);



        World.startEvent(e -> {

            while(true) {
                e.delay(17); //60 seconds
                if (shopkeep == null)
                    continue;

                if (Deadman.getStage()instanceof Lobby || Deadman.getStage() instanceof Final) {
                    if (shopkeep.getId() != SHOPKEEPER_IDS[0]) {
                        shopkeep.setId(SHOPKEEPER_IDS[0]);
                        World.players.forEach(p -> { p.sendMessage(Color.YELLOW.wrap("[Tournament] ") + "The Citadel Shop just got downgraded to Tier 1!"); });
                    }
                    continue;
                }

                //MUST BE GAME!
                Main main = (Main) Deadman.getStage();
                int percent = main.getRuntimePercentage();
                System.out.println("Current %: " + percent);

                if (percent >= 75 && shopkeep.getId() != SHOPKEEPER_IDS[3]) {
                    killShopkeep(e);
                    spawnShopkeep(SHOPKEEPER_IDS[3]);
                    World.players.forEach(p -> { p.sendMessage(Color.YELLOW.wrap("[Tournament] ") + "The Citadel Shop just got upgraded to Tier 4!"); });
                } else if (percent >= 55 && shopkeep.getId() != SHOPKEEPER_IDS[2]) {
                    killShopkeep(e);
                    spawnShopkeep(SHOPKEEPER_IDS[2]);
                    World.players.forEach(p -> { p.sendMessage(Color.YELLOW.wrap("[Tournament] ") + "The Citadel Shop just got upgraded to Tier 3!"); });
                } else if (percent >= 35 && shopkeep.getId() != SHOPKEEPER_IDS[1]) {
                    killShopkeep(e);
                    spawnShopkeep(SHOPKEEPER_IDS[1]);
                    World.players.forEach(p -> { p.sendMessage(Color.YELLOW.wrap("[Tournament] ") + "The Citadel Shop just got upgraded to Tier 2!"); });
                }
            }
        });

    }

    private static void killShopkeep(Event e) {
        shopkeep.hit(new Hit(HitType.DAMAGE).fixedDamage(99));
        e.delay(6);
        shopkeep.remove();
    }

    private static void spawnShopkeep(int id) {
        shopkeep = new NPC(id);
        shopkeep.spawn(2961, 3335, 0, Direction.NORTH, 0);
    }

}
