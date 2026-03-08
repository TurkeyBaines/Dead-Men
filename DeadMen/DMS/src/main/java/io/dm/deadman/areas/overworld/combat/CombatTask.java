package io.dm.deadman.areas.overworld.combat;

import io.dm.api.utils.Random;
import io.dm.cache.ItemDef;
import io.dm.cache.NPCDef;
import io.dm.cache.NpcID;
import io.dm.deadman.areas.overworld.combat.tasks.ChickenTask;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.shared.listeners.DeathListener;
import io.dm.model.inter.actions.SimpleAction;
import io.dm.model.inter.dialogue.NPCDialogue;
import io.dm.model.map.Bounds;
import io.dm.model.map.MapListener;
import io.dm.model.map.ground.GroundItem;

import java.util.ArrayList;

public abstract class CombatTask {

    private static ArrayList<CombatTask> tasks;

    public abstract int[] NPC_ID();
    public abstract int[] DIFF_QUANTITY();
    public abstract Bounds LOCATION();
    public abstract int NUM_NPC_TO_SPAWN();

    public int remaining;
    public int total;

    public boolean isTaskNPC(Player p , int id) {
        for (int i : NPC_ID())
            if (i == id)
                return true;

        return false;
    }

    public CombatTask(Player p, int difficulty) {
        int amount = DIFF_QUANTITY()[difficulty];
        p.overworldTaskRemaining = amount;
        p.overworldTaskTotal = amount;
    }

    public CombatTask() {}

    public void deplete(Player p) {
        p.overworldTaskRemaining--;

        if (p.overworldTaskRemaining <= 0) {
            //Task Complete
            complete(p);
        }
    }

    public void complete(Player p) {
        p.overworldTaskMonster = TASK_MONSTER.NONE;
        p.overworldTaskTotal = -1;
        p.overworldTaskRemaining = -1;
        p.sendMessage("Congratulations, you've completed your task! Head back to the Notice Board to get another.");
    }

    public void spawn() {
        for (int i = 0; i < NUM_NPC_TO_SPAWN(); i++) {
            NPC n = new NPC(NPC_ID()[Random.get(NPC_ID().length-1)]);
            n.spawnBounds = LOCATION();
            n.walkBounds = LOCATION();
            n.getDef().combatInfo.respawn_ticks = 5;
            if (n.getCombat() == null) {
                System.out.println("NULL COMBAT: " + n.getId());
            }
            n.spawn(LOCATION().randomPosition());

            n.deathEndListener = (DeathListener.SimpleKiller) killer -> {
                if(killer == null)
                    return;
                if (killer.player == null)
                    return;
                Player p = killer.player;
                if (p.overworldTaskMonster == TASK_MONSTER.NONE)
                    return;
                if (!isTaskNPC(p, n.getId()))
                    return;

                deplete(p);
            };

        }
    }


    static {

        tasks = new ArrayList<>();
        tasks.add(new ChickenTask());

        for (CombatTask ct : tasks)
            ct.spawn();

        MapListener.registerRegion(11165).onEnter(CombatTask::onEnter).onExit(CombatTask::onExit);

        // SPAWN THE NPCs IN THE CAVE HERE!
        // ENSURE TO SET IT SO THEY DON'T AGRO ONTO PEOPLE!
        // IF PLAYERS DIE, THEY SHOULD SPAWN BACK OUTSIDE THE CAVE
        //  AND THEN THE COMBAT TASK SHOULD CLEAR (THEY FAIL)

    }

    static void onEnter(Player p) {
        p.attackNpcListener = (p1, n1, msg) -> {

            if (p.overworldTaskMonster == TASK_MONSTER.NONE) {
                p.dialogue(new NPCDialogue(NpcID.DEATH, "You don't have a task, head back and see the Notice Board to begin"));
                return false;
            }

            if (getClassForTask(p.overworldTaskMonster).isTaskNPC(p1, n1.getId()))
                return true;

            p.dialogue(
                    new NPCDialogue(NpcID.DEATH, "Get off those monsters, you haven't been asked to kill them!"),
                    new NPCDialogue(NpcID.DEATH, p.overworldTaskMonster == TASK_MONSTER.NONE ? "You don't have a task, head back and see the Notice Board to begin" : "Your task is to kill " + p.overworldTaskRemaining + " x " + NPCDef.get(getClassForTask(p.overworldTaskMonster).NPC_ID()[0]))
            );
            return false;
        };

        p.attackPlayerListener = (p1, p2, msg) -> false;
    }
    static void onExit(Player p, boolean logout) {
        p.attackNpcListener = null;
        p.attackPlayerListener = (p1, p2, msg) -> false;
    }

    static CombatTask getClassForTask(TASK_MONSTER tm) {
        switch (tm) {
            case CHICKEN: return tasks.get(ChickenTask.ID);

            default: return null;
        }
    }

    public enum TASK_MONSTER {
        NONE("None", -1),
        CHICKEN("Chickens", 3);

        String name;
        int cbUnlock;
        TASK_MONSTER(String Name, int CombatLevelUnlock) {
            this.name = Name;
            this.cbUnlock = CombatLevelUnlock;
        }

        public void assign(Player p, int diff) {
            switch (this) {
                case CHICKEN -> new ChickenTask(p, diff);
            }

            p.dialogue(
                    new NPCDialogue(
                            new NPC(2713),
                            "Please go and kill " + p.overworldTaskRemaining + " " + name
                    )
            );
        }
    }

    public static boolean hasTask(Player p) {
        return p.overworldTaskMonster != TASK_MONSTER.NONE;
    }

    public static TASK_MONSTER getNewTask(Player p) {
        int cblvl = p.getCombat().getLevel();

        ArrayList<TASK_MONSTER> holder = new ArrayList<>();
        for (TASK_MONSTER tm : TASK_MONSTER.values()) {
            if (tm == TASK_MONSTER.NONE) continue;

            if (cblvl >= tm.cbUnlock)
                holder.add(tm);
        }
        return holder.get(Random.get(holder.size()-1));
    }

}
