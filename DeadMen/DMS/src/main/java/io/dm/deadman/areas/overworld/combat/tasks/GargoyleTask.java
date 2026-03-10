package io.dm.deadman.areas.overworld.combat.tasks;

import io.dm.api.utils.Random;
import io.dm.deadman.areas.overworld.combat.CombatTask;
import io.dm.model.entity.player.Player;
import io.dm.model.map.Bounds;

public class GargoyleTask extends CombatTask {

    public static final int ID = 4;

    public GargoyleTask(Player p, int difficulty) {
        super(p, difficulty);
        p.overworldTaskMonster = TASK_MONSTER.GARGOYLE;

        p.sendMessage("Task accepted!");
        p.sendMessage("  - NPC: Gargoyles");
        p.sendMessage("  - Amount: " + p.overworldTaskRemaining);
    }

    public GargoyleTask() {
        super();
    }

    @Override
    public int[] NPC_ID() {
        return new int[] {
                424
        };
    }

    @Override
    public int[] DIFF_QUANTITY() {
        return new int[] {
                Random.get(20, 35),
                Random.get(50, 75),
                Random.get(125, 140)
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
                { 2764, 10104 },
                { 2771, 10104 },
                { 2774, 10100 },
                { 2769, 10094 },
                { 2762, 10094 }
        }, 0);
    }

    @Override
    public int NUM_NPC_TO_SPAWN() {
        return 5;
    }
}
