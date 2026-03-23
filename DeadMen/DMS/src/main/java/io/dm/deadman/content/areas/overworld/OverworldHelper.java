package io.dm.deadman.content.areas.overworld;

import io.dm.model.entity.player.Player;
import io.dm.model.entity.shared.LockType;
import io.dm.model.item.Item;
import io.dm.model.item.ItemContainer;
import io.dm.model.stat.StatType;

public class OverworldHelper {

    public static void process(Player p) {

        p.lock(LockType.FULL);

        // == Stats ==
        int[][] ostats = p.skillHolder;
        int[][] holder = new int[StatType.values().length][2];
        for (int i = 0; i < StatType.values().length; i++) {
            holder[i][0] = p.getStats().get(StatType.get(i)).fixedLevel;
            holder[i][1] = (int) p.getStats().get(StatType.get(i)).experience;
            p.getStats().set(StatType.get(i), ostats[i][0], ostats[i][1]);
        }
        p.skillHolder = holder;

        // == Inventory ==
        ItemContainer holder2 = new ItemContainer();
        holder2.init(28, false);
        for (Item i : p.getInventory().getItems()) {
            if (i == null) continue;
            holder2.add(i);
            p.getInventory().remove(i);
        }
        if (p.inventoryHolder != null && p.inventoryHolder.getItems() != null)
            for (Item i : p.inventoryHolder.getItems()) {
                if (i == null) continue;

                p.getInventory().add(i);
            }
        p.inventoryHolder = holder2;

        // == Equipment ==
        Item[] equipment = new Item[14];
        for (int i = 0; i < 14; i++) {
            if (i == 6 || i == 8 || i == 11) continue;
            equipment[i] = p.getEquipment().get(i);
            if (p.equipmentHolder != null && p.equipmentHolder[i] != null)
                p.getEquipment().set(i, p.equipmentHolder[i]);
            else
                p.getEquipment().set(i, null);
        }
        p.equipmentHolder = equipment;

        p.unlock();

    }

}
