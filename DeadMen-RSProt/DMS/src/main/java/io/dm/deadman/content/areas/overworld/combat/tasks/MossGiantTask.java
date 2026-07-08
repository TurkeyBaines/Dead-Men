package io.dm.deadman.content.areas.overworld.combat.tasks;

import io.dm.api.utils.Random;
import io.dm.deadman.content.areas.overworld.combat.CombatTask;
import io.dm.model.entity.player.Player;
import io.dm.model.map.Bounds;

public class MossGiantTask extends CombatTask {

    public static final int ID = 2;

    public MossGiantTask(Player p, int difficulty) {
        super(p, difficulty);
        p.overworldTaskMonster = TASK_MONSTER.ROCK_CRAB;

        p.sendMessage("Task accepted!");
        p.sendMessage("  - NPC: Moss Giants");
        p.sendMessage("  - Amount: " + p.overworldTaskRemaining);
    }

    public MossGiantTask() {
        super();
    }

    @Override
    public int[] NPC_ID() {
        return new int[] {
                2109
        };
    }

    @Override
    public int[] DIFF_QUANTITY() {
        return new int[] {
                Random.get(30, 40),
                Random.get(75, 95),
                Random.get(145, 185)
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
                        { 2803, 10071 },
                        { 2800, 10073 },
                        { 2797, 10070 },
                        { 2799, 10064 },
                        { 2795, 10057 },
                        { 2801, 10053 },
                        { 2804, 10061 }},
                0
        );
    }

    @Override
    public int NUM_NPC_TO_SPAWN() {
        return 5;
    }
}
