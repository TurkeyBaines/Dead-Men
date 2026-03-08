package io.dm.deadman.areas.Miscellania;

import io.dm.api.utils.Random;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.item.loot.LootItem;
import io.dm.model.item.loot.LootTable;
import io.dm.model.map.object.actions.ObjectAction;
import io.dm.model.skills.herblore.Herb;
import io.dm.model.stat.StatType;

import java.util.ArrayList;

public class OverworldActions {

    static {

        /*
            Herblore Patch
         */
        ObjectAction.register(35834, "Pick", (p, o) -> {
            p.startEvent(e -> {
                while(true) {
                    p.animate(2282);
                    e.delay(4);
                    p.sendMessage("Rolling....");
                    if (Random.get(3) == 1) {
                        p.sendMessage("We Won!");

                        Item i = getHerb(p);
                        double xp = 0;
                        for (Herb h : Herb.values())
                            if (h.grimyId == i.getId())
                                xp = h.xp * 4;
                        p.getInventory().addOrDrop(getHerb(p));


                        p.getStats().addXp(StatType.Farming, xp, true);
                    }
                    e.delay(1);
                }
            });
        });

    }

    private static Item getHerb(Player p) {
        int level = p.getStats().get(StatType.Farming).currentLevel;
        ArrayList<LootItem> herbs = new ArrayList<>();
        for (Herb h : Herb.values())
            if (level >= h.lvlReq || h == Herb.GUAM_LEAF)
                herbs.add(new LootItem(h.grimyId, 1, h.lvlReq));

        LootTable lootTable = new LootTable().addTable(1, herbs.toArray(LootItem[]::new));
        return lootTable.rollItem();
    }

}
