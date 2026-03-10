package io.dm.deadman.areas.overworld.combat.tasks;

import io.dm.api.utils.Random;
import io.dm.deadman.areas.overworld.combat.CombatTask;
import io.dm.model.entity.player.Player;
import io.dm.model.map.Bounds;

public class RockCrabTask extends CombatTask {

    public static final int ID = 1;

    public RockCrabTask(Player p, int difficulty) {
        super(p, difficulty);
        p.overworldTaskMonster = TASK_MONSTER.ROCK_CRAB;

        p.sendMessage("Task accepted!");
        p.sendMessage("  - NPC: Rock Crabs");
        p.sendMessage("  - Amount: " + p.overworldTaskRemaining);
    }

    public RockCrabTask() {
        super();
    }

    @Override
    public int[] NPC_ID() {
        return new int[] {
                119
        };
    }

    @Override
    public int[] DIFF_QUANTITY() {
        return new int[] {
                Random.get(30, 50),
                Random.get(80, 120),
                Random.get(170, 230)
        };
    }

    @Override
    public double[] DIFF_MULTIPLIER() {
        return new double[]{
                0.8,
                1.0,
                1.2
        };
    }

    @Override
    public Bounds LOCATION() {
        return new Bounds(
                new int[][]{
                        { 2773, 10069 },
                        { 2778, 10073 },
                        { 2785, 10070 },
                        { 2782, 10062 },
                        { 2774, 10061 }},
                0
        );
    }

    @Override
    public int NUM_NPC_TO_SPAWN() {
        return 8;
    }
}
