package io.dm.deadman.content.loot;

import io.dm.model.item.Item;
import io.dm.model.item.loot.LootItem;
import io.dm.model.item.loot.LootTable;

import java.text.DecimalFormat;
import java.util.List;

public class GlobalDropManager {

    // Define the 4 Tiered Tables
    private static final LootTable LOW_TIER = new LootTable().addTable(1,
            new LootItem(1167, 1, 10), // Leather body
            new LootItem(1323, 1, 5)   // Iron scimitar
    );
    private static final LootTable MED_TIER = new LootTable().addTable(1,
            new LootItem(1163, 1, 10), // Rune full helm
            new LootItem(1333, 1, 5)   // Rune scimitar
    );
    private static final LootTable HIGH_TIER = new LootTable().addTable(1,
            new LootItem(1215, 1, 5),  // Dragon dagger
            new LootItem(4087, 1, 2)   // Dragon platelegs
    );
    private static final LootTable ELITE_TIER = new LootTable().addTable(1,
            new LootItem(4151, 1, 1),  // Abyssal whip
            new LootItem(4716, 1, 1)   // Dharok's greataxe
    );

    public static Item getDrop(int npcLevel) {
        // 1. Calculate Weights
        int nothingWeight = 500;
        int lowWeight = 100;
        int medWeight = (npcLevel > 50) ? (npcLevel - 40) : 0;
        int highWeight = (npcLevel > 80) ? (npcLevel - 70) : 0;
        int eliteWeight = (npcLevel > 110) ? (npcLevel - 100) : 0;

        // 2. Manual "Nothing" Check
        double totalWeight = nothingWeight + lowWeight + medWeight + highWeight + eliteWeight;
        // Using your project's Random utility
        if (io.dm.api.utils.Random.get() * totalWeight < nothingWeight) {
            return null;
        }

        // 3. If we passed the nothing check, build the dynamic table for the rest
        LootTable dynamicTable = new LootTable();

        if (lowWeight > 0)
            dynamicTable.addTable("Low", lowWeight, LOW_TIER.getLootItems().toArray(new LootItem[0]));
        if (medWeight > 0)
            dynamicTable.addTable("Med", medWeight, MED_TIER.getLootItems().toArray(new LootItem[0]));
        if (highWeight > 0)
            dynamicTable.addTable("High", highWeight, HIGH_TIER.getLootItems().toArray(new LootItem[0]));
        if (eliteWeight > 0)
            dynamicTable.addTable("Elite", eliteWeight, ELITE_TIER.getLootItems().toArray(new LootItem[0]));

        dynamicTable.calculateWeight();

        // 4. Return the result (will always be an item because we already bypassed 'Nothing')
        return dynamicTable.rollItem();
    }

}