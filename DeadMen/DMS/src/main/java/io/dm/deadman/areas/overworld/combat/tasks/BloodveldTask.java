package io.dm.deadman.areas.overworld.combat.tasks;

import io.dm.api.utils.Random;
import io.dm.deadman.areas.overworld.combat.CombatTask;
import io.dm.model.entity.player.Player;
import io.dm.model.map.Bounds;

public class BloodveldTask extends CombatTask {

    public static final int ID = 3;

    public BloodveldTask(Player p, int difficulty) {
        super(p, difficulty);
        p.overworldTaskMonster = TASK_MONSTER.BLOODVELD;

        p.sendMessage("Task accepted!");
        p.sendMessage("  - NPC: Bloodvelds");
        p.sendMessage("  - Amount: " + p.overworldTaskRemaining);
    }

    public BloodveldTask() {
        super();
    }

    @Override
    public int[] NPC_ID() {
        return new int[] {
                490
        };
    }

    @Override
    public int[] DIFF_QUANTITY() {
        return new int[] {
                Random.get(25, 35),
                Random.get(60, 90),
                Random.get(140, 165)
        };
    }

    @Override
    public double[] DIFF_MULTIPLIER() {
        return new double[] {
                0.8,
                1.0,
                1.2
        };
    }

    @Override
    public Bounds LOCATION() {
        return new Bounds(new int[][]{
                { 2780, 10107 },
                { 2785, 10107 },
                { 2787, 10102 },
                { 2785, 10096 },
                { 2780, 10102 },
                { 2774, 10102 }
        }, 0);
    }

    @Override
    public int NUM_NPC_TO_SPAWN() {
        return 5;
    }
}
