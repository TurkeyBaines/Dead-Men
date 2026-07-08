package io.dm.deadman.content.loot;

import io.dm.api.utils.Random;
import io.dm.deadman.Deadman;
import io.dm.deadman.tournament.TournamentConfig;
import io.dm.deadman.util.DMMConst;
import io.dm.model.item.Item;
import io.dm.model.item.loot.LootItem;
import io.dm.model.item.loot.LootTable;

/**
 * GLOBAL LOOT TABLE
 * -----------------
 * Fires on EVERY npc kill, ON TOP of that npc's own drop table, as a bonus roll
 * (see {@code NPCCombat.dropItems}). Its job is the Game Design Bible's
 * "every kill gives a little something" layer, not to replace boss/npc tables.
 *
 * Balance philosophy (from docs/Game-Design-Bible.md + docs/Tournament-Design.md):
 *   - Pillar 5 (Respect the player's time): most kills yield a useful SUPPLY
 *     (food / runes / ammo / coins) so the map always feels alive. No more 66% "nothing".
 *   - Pillar 2 (Skill over stuff): gear tops out at "almost-BiS". TRUE best-in-slot
 *     (whip, godswords, ancient/imbued gear) lives on EVENT reward tables only -
 *     a trash mob must never be able to hand someone a win.
 *   - Better gear tiers UNLOCK as NPC combat level rises; the toughest mobs are the
 *     only source of the Elite tier, and even then it is rare.
 *   - The per-tournament DROP_RATE config multiplier now matters: it shrinks the
 *     "nothing" weight, so a high-drop tournament feels noticeably more generous.
 *
 * Tuning lives entirely in this file. Intra-tier weights = relative rarity within a
 * tier (higher weight = more common). Cross-tier weights are computed in getDrop().
 */
public class GlobalDropManager {

    /* ----------------------------------------------------------------------
     * SUPPLY TIER  -  the heartbeat of the table. Keeps players fed, stocked
     * and in the fight. This is what makes the map feel alive.
     * ---------------------------------------------------------------------- */
    private static final LootTable SUPPLY_TIER = new LootTable().addTable(1,
            // Food
            new LootItem(379, 2, 5, 18),    // Lobster
            new LootItem(373, 2, 4, 14),    // Swordfish
            new LootItem(385, 1, 3, 8),     // Shark
            new LootItem(7946, 2, 4, 10),   // Monkfish
            // Potions
            new LootItem(2434, 1, 2, 8),    // Prayer potion(4)
            new LootItem(2440, 1, 1, 6),    // Super strength(4)
            new LootItem(2436, 1, 1, 6),    // Super attack(4)
            new LootItem(2442, 1, 1, 6),    // Super defence(4)
            new LootItem(2444, 1, 1, 5),    // Ranging potion(4)
            new LootItem(3040, 1, 1, 5),    // Magic potion(4)
            new LootItem(3024, 1, 1, 4),    // Super restore(4)
            // Runes (caster fuel)
            new LootItem(556, 100, 300, 14),// Air rune
            new LootItem(555, 100, 300, 10),// Water rune
            new LootItem(557, 100, 300, 10),// Earth rune
            new LootItem(554, 100, 300, 10),// Fire rune
            new LootItem(562, 50, 150, 10), // Chaos rune
            new LootItem(560, 30, 90, 7),   // Death rune
            new LootItem(565, 20, 60, 5),   // Blood rune
            new LootItem(561, 30, 90, 7),   // Nature rune
            new LootItem(563, 30, 90, 7),   // Law rune
            // Ammunition
            new LootItem(888, 100, 300, 10),// Mithril arrow
            new LootItem(890, 75, 200, 8),  // Adamant arrow
            new LootItem(892, 50, 150, 6),  // Rune arrow
            // Currency
            new LootItem(995, 1000, 5000, 16),               // Coins
            new LootItem(DMMConst.BLOOD_MONEY, 50, 200, 6)   // Blood Money
    );

    /* ----------------------------------------------------------------------
     * LOW TIER  -  starter -> early game. Gets a fresh player off the ground.
     * ---------------------------------------------------------------------- */
    private static final LootTable LOW_TIER = new LootTable().addTable(1,
            // Melee armour
            new LootItem(1115, 1, 1, 10),   // Iron platebody
            new LootItem(1119, 1, 1, 8),    // Steel platebody
            new LootItem(1067, 1, 1, 10),   // Iron platelegs
            new LootItem(1069, 1, 1, 8),    // Steel platelegs
            new LootItem(1153, 1, 1, 9),    // Iron full helm
            new LootItem(1157, 1, 1, 7),    // Steel full helm
            new LootItem(1191, 1, 1, 7),    // Iron kiteshield
            new LootItem(1193, 1, 1, 6),    // Steel kiteshield
            // Weapons
            new LootItem(1323, 1, 1, 10),   // Iron scimitar
            new LootItem(1325, 1, 1, 8),    // Steel scimitar
            new LootItem(1327, 1, 1, 5),    // Black scimitar
            // Ranged / Magic starters
            new LootItem(843, 1, 1, 6),     // Oak shortbow
            new LootItem(849, 1, 1, 5),     // Willow shortbow
            new LootItem(1381, 1, 1, 5),    // Staff of air
            new LootItem(1387, 1, 1, 5)     // Staff of fire
    );

    /* ----------------------------------------------------------------------
     * MED TIER  -  the mid-game setup. Adamant, mystic, green d'hide.
     * ---------------------------------------------------------------------- */
    private static final LootTable MED_TIER = new LootTable().addTable(1,
            // Adamant melee
            new LootItem(1123, 1, 1, 9),    // Adamant platebody
            new LootItem(1073, 1, 1, 9),    // Adamant platelegs
            new LootItem(1161, 1, 1, 8),    // Adamant full helm
            new LootItem(1199, 1, 1, 6),    // Adamant kiteshield
            new LootItem(1331, 1, 1, 9),    // Adamant scimitar
            new LootItem(1329, 1, 1, 7),    // Mithril scimitar
            // Mystic (mage)
            new LootItem(4089, 1, 1, 7),    // Mystic hat
            new LootItem(4091, 1, 1, 6),    // Mystic robe top
            new LootItem(4093, 1, 1, 6),    // Mystic robe bottom
            new LootItem(1391, 1, 1, 5),    // Battlestaff
            // Green d'hide (ranged)
            new LootItem(1135, 1, 1, 7),    // Green d'hide body
            new LootItem(1099, 1, 1, 7),    // Green d'hide chaps
            new LootItem(1065, 1, 1, 6),    // Green d'hide vambraces
            new LootItem(851, 1, 1, 6),     // Maple shortbow
            new LootItem(855, 1, 1, 4)      // Yew shortbow
    );

    /* ----------------------------------------------------------------------
     * HIGH TIER  -  high game. Rune, dragon weapons, blue/red d'hide, msb.
     * ---------------------------------------------------------------------- */
    private static final LootTable HIGH_TIER = new LootTable().addTable(1,
            // Rune melee
            new LootItem(1127, 1, 1, 8),    // Rune platebody
            new LootItem(1079, 1, 1, 8),    // Rune platelegs
            new LootItem(1163, 1, 1, 7),    // Rune full helm
            new LootItem(1201, 1, 1, 6),    // Rune kiteshield
            new LootItem(1333, 1, 1, 8),    // Rune scimitar
            new LootItem(1319, 1, 1, 5),    // Rune 2h sword
            // Dragon weapons
            new LootItem(1215, 1, 1, 5),    // Dragon dagger
            new LootItem(4587, 1, 1, 4),    // Dragon scimitar
            new LootItem(1434, 1, 1, 4),    // Dragon mace
            new LootItem(1305, 1, 1, 3),    // Dragon longsword
            // Ranged
            new LootItem(2499, 1, 1, 5),    // Blue d'hide body
            new LootItem(2493, 1, 1, 5),    // Blue d'hide chaps
            new LootItem(861, 1, 1, 5),     // Magic shortbow
            new LootItem(9185, 1, 1, 3),    // Rune crossbow
            // Amulets / supplies that gate high play
            new LootItem(1725, 1, 1, 5),    // Amulet of strength
            new LootItem(1727, 1, 1, 4)     // Amulet of magic
    );

    /* ----------------------------------------------------------------------
     * ELITE TIER  -  "almost-BiS". The rare jackpot from the toughest mobs.
     * NOTE: deliberately NO whip / godswords / ancient gear - those are
     * reserved for EVENT reward tables (Pillar 2: skill over stuff).
     * ---------------------------------------------------------------------- */
    private static final LootTable ELITE_TIER = new LootTable().addTable(1,
            // Dragon armour
            new LootItem(4087, 1, 1, 8),    // Dragon platelegs
            new LootItem(4585, 1, 1, 8),    // Dragon plateskirt
            new LootItem(3140, 1, 1, 6),    // Dragon chainbody
            new LootItem(1149, 1, 1, 6),    // Dragon med helm
            new LootItem(1187, 1, 1, 5),    // Dragon sq shield
            new LootItem(11840, 1, 1, 6),   // Dragon boots
            // Best-in-global accessories (un-imbued rings, strong ammys)
            new LootItem(6737, 1, 1, 3),    // Berserker ring
            new LootItem(6733, 1, 1, 3),    // Archers ring
            new LootItem(6731, 1, 1, 3),    // Seers ring
            new LootItem(6585, 1, 1, 2),    // Amulet of fury
            // Iconic almost-BiS armour pieces
            new LootItem(10828, 1, 1, 4),   // Helm of neitiznot
            new LootItem(10551, 1, 1, 3)    // Fighter torso
    );

    /**
     * Rolls the global bonus drop for a single kill.
     *
     * @param npcLevel the slain npc's combat level - gates which tiers can appear.
     * @return a bonus {@link Item}, or {@code null} for "nothing".
     */
    public static Item getDrop(int npcLevel) {
        int dropRate = currentDropRate();

        /*
         * 1. Cross-tier weights.
         *    - SUPPLY dominates so most kills feed the player (Pillar 5).
         *    - Gear tiers unlock by npc level and are capped so the best stuff stays
         *      rare even on max-level mobs.
         *    - "nothing" shrinks as DROP_RATE rises (min floor keeps it meaningful).
         */
        int nothingWeight = Math.max(40, 200 / dropRate);
        int supplyWeight = 300;
        int lowWeight = 150;
        int medWeight = (npcLevel > 50) ? Math.min(npcLevel - 45, 120) : 0;
        int highWeight = (npcLevel > 90) ? Math.min(npcLevel - 85, 70) : 0;
        int eliteWeight = (npcLevel > 120) ? Math.min(npcLevel - 115, 25) : 0;

        /* 2. Manual "nothing" check before building the table. */
        double totalWeight = nothingWeight + supplyWeight + lowWeight + medWeight + highWeight + eliteWeight;
        if (Random.get() * totalWeight < nothingWeight) {
            return null;
        }

        /* 3. Build the dynamic table from the tiers that survived the level gate. */
        LootTable dynamicTable = new LootTable();
        dynamicTable.addTable("Supply", supplyWeight, SUPPLY_TIER.getLootItems().toArray(new LootItem[0]));
        dynamicTable.addTable("Low", lowWeight, LOW_TIER.getLootItems().toArray(new LootItem[0]));
        if (medWeight > 0)
            dynamicTable.addTable("Med", medWeight, MED_TIER.getLootItems().toArray(new LootItem[0]));
        if (highWeight > 0)
            dynamicTable.addTable("High", highWeight, HIGH_TIER.getLootItems().toArray(new LootItem[0]));
        if (eliteWeight > 0)
            dynamicTable.addTable("Elite", eliteWeight, ELITE_TIER.getLootItems().toArray(new LootItem[0]));

        dynamicTable.calculateWeight();

        /* 4. We already passed the nothing check, so this returns a real item. */
        return dynamicTable.rollItem();
    }

    /** Reads the active tournament's DROP_RATE, defaulting safely to 1x. */
    private static int currentDropRate() {
        TournamentConfig config = Deadman.getConfig();
        if (config == null || config.DROP_RATE < 1)
            return 1;
        return config.DROP_RATE;
    }

}
