package io.dm.deadman.areas.overworld.combat.tasks;

import io.dm.cache.NpcID;
import io.dm.deadman.areas.overworld.combat.CombatTask;
import io.dm.model.entity.player.Player;
import io.dm.model.map.Bounds;

public class DragonTask extends CombatTask {

    public static final int ID = 1;

    public DragonTask(Player p, int diff) {
        super(p, diff);
        p.overworldTaskMonster = TASK_MONSTER.DRAGON;

        p.sendMessage("Task accepted!");
        p.sendMessage("  - NPC: Dragon");
        p.sendMessage("  - Amount: " + p.overworldTaskRemaining);
    }

    public DragonTask() {
        super();
    }

    @Override
    public int[] NPC_ID() {
        return new int[] {
                NpcID.IRON_DRAGON,
                NpcID.IRON_DRAGON_273
        };
    }

    @Override
    public int[] DIFF_QUANTITY() {
        return new int[] {
            10, 25, 40
        };
    }

    @Override
    public Bounds LOCATION() {
        return new Bounds(new int[][]{
                {2766, 10102},
                {2775, 10103},
                {2778, 10094},
                {2767, 10092},
                {2764, 10097}}, 0);
    }

    @Override
    public int NUM_NPC_TO_SPAWN() {
        return 4;
    }
}
