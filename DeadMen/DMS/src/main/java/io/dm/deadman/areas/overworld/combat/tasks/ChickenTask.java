package io.dm.deadman.areas.overworld.combat.tasks;

import io.dm.api.utils.Random;
import io.dm.cache.NpcID;
import io.dm.deadman.areas.overworld.combat.CombatTask;
import io.dm.model.entity.player.Player;
import io.dm.model.map.Bounds;
import io.dm.model.map.Position;

public class ChickenTask extends CombatTask {

    public static int ID = 0;

    public ChickenTask(Player p, int difficulty) {
        super(p, difficulty);
        p.overworldTaskMonster = TASK_MONSTER.CHICKEN;

        p.sendMessage("Task accepted!");
        p.sendMessage("  - NPC: Chickens");
        p.sendMessage("  - Amount: " + p.overworldTaskRemaining);
    }

    public ChickenTask() {
        super();
    }

    @Override
    public int[] NPC_ID() {
        return new int[] {
                NpcID.CHICKEN,
                NpcID.CHICKEN_1174,
                NpcID.CHICKEN_2804,
                NpcID.CHICKEN_2805,
                NpcID.CHICKEN_2806
        };
    }

    @Override
    public int[] DIFF_QUANTITY() {
        return new int[] {
                Random.get(40, 60),
                Random.get(130, 170),
                Random.get(270, 330)
        };
    }

    @Override
    public double[] DIFF_MULTIPLIER() {
        return new double[] {
                0.8, 1.0, 1.2
        };
    }

//    path.add(new WorldPoint(2772, 10079, 0));
//path.add(new WorldPoint(2781, 10078, 0));
//path.add(new WorldPoint(2782, 10070, 0));
//path.add(new WorldPoint(2774, 10066, 0));
//path.add(new WorldPoint(2770, 10072, 0));

    @Override
    public Bounds LOCATION() {
        return new Bounds(new int[][] {
                {2772, 10079},
                {2781, 10078},
                {2782, 10070},
                {2774, 10066},
                {2770, 10072},
        }, 0);
    }

    @Override
    public int NUM_NPC_TO_SPAWN() {
        return 10;
    }
}
