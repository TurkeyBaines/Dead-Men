package io.dm.deadman.areas.overworld;

import io.dm.api.utils.Random;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.item.loot.LootItem;
import io.dm.model.item.loot.LootTable;
import io.dm.model.map.Direction;
import io.dm.model.map.Position;
import io.dm.model.map.object.GameObject;
import io.dm.model.map.object.actions.ObjectAction;
import io.dm.model.skills.herblore.Herb;
import io.dm.model.stat.StatType;

import java.util.ArrayList;

public class OverworldObjects {

    static {

        /*
            ZMI Altar
         */
        GameObject altar = new GameObject(29631, new Position(2517, 3883, 0), 10, 0);
        GameObject glowPillarSW = new GameObject(34794, new Position(2516, 3882, 0), 10, 0);
        GameObject glowPillarSE = new GameObject(34794, new Position(2520, 3882, 0), 10, 0);
        GameObject glowPillarNE = new GameObject(34794, new Position(2520, 3886, 0), 10, 0);
        GameObject glowPillarNW = new GameObject(34794, new Position(2516, 3886, 0), 10, 0);
        altar.spawn();
        glowPillarSW.spawn();
        glowPillarSE.spawn();
        glowPillarNE.spawn();
        glowPillarNW.spawn();


        /*
            Herblore Patch
         */
        ObjectAction.register(35834, "Pick", (p, o) -> {
            p.startEvent(e -> {
                while(true) {
                    p.animate(2282);
                    e.delay(4);
                    handleHerbSpot(p);
                    e.delay(1);
                }
            });
        });

        ObjectAction.register(34499, "Enter", (p, o) -> {
            p.getMovement().teleport(2759, 10064);
            p.face(Direction.EAST);
        });

        ObjectAction.register(5859, "Exit", (p, o) -> {
            p.getMovement().teleport(2615, 3853);
            p.face(Direction.WEST);
        });

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
}
