package io.dm.deadman.content.areas.overworld.combat;

import io.dm.api.utils.Random;
import io.dm.cache.NPCDef;
import io.dm.cache.NpcID;
import io.dm.deadman.content.areas.overworld.combat.tasks.*;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.shared.listeners.DeathListener;
import io.dm.model.inter.dialogue.NPCDialogue;
import io.dm.model.map.Bounds;
import io.dm.model.map.Direction;
import io.dm.model.map.MapListener;
import io.dm.model.stat.StatType;

import java.util.ArrayList;

public abstract class CombatTask {

    private static ArrayList<CombatTask> tasks;

    public abstract int[] NPC_ID();
    public abstract int[] DIFF_QUANTITY();
    public abstract double[] DIFF_MULTIPLIER();
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

    public void addPoints(Player p) {
        int hp = NPCDef.get(NPC_ID()[0]).combatInfo.hitpoints;
        int slaylvl = p.overworldTaskMonster.slayUnlock;
        double multi = DIFF_MULTIPLIER()[p.overworldTaskDifficulty];
        int points = PointCalculator.getFinalPoints(hp, multi, slaylvl);
        p.overworldPoints += p.overworldPointsVault;
        p.sendFilteredMessage("You are awarded " + p.overworldPointsVault + " Overworld Points for completing the task, you now have " + p.overworldPoints + " Points.");
        p.overworldPointsVault = 0;
    }

    public void addPointsToVault(Player p) {
        int hp = NPCDef.get(NPC_ID()[0]).combatInfo.hitpoints;
        int slaylvl = p.overworldTaskMonster.slayUnlock;
        double multi = DIFF_MULTIPLIER()[p.overworldTaskDifficulty];
        int points = PointCalculator.getFinalPoints(hp, multi, slaylvl);
        p.overworldPointsVault += points;
        p.sendFilteredMessage("Task Vault: " + p.overworldPointsVault + "(+ " + points + ")");
    }

    public void giveXP(Player p) {
        int xp = NPCDef.get(NPC_ID()[0]).combatInfo.hitpoints;
        p.getStats().addXp(StatType.Slayer, xp, true);
    }

    public void complete(Player p) {
        p.sendMessage("Congratulations, you've completed your task! Head back to the Notice Board to get another.");
        addPoints(p);
        p.sendMessage("You receive an additional " + (p.overworldTaskDifficulty == 0 ? "50 " : p.overworldTaskDifficulty == 1 ? "250" : "500") + " Overworld points for difficulty.");
        p.overworldPoints += p.overworldTaskDifficulty == 0 ? 50 : p.overworldTaskDifficulty == 1 ? 250 : 500;
        p.overworldTaskMonster = TASK_MONSTER.NONE;
        p.overworldTaskTotal = -1;
        p.overworldTaskRemaining = -1;
        p.overworldTaskDifficulty = -1;
        p.str_overworldTaskDifficulty = "None";
    }

    public void spawn() {
        for (int i = 0; i < NUM_NPC_TO_SPAWN(); i++) {
            NPC n = new NPC(NPC_ID()[Random.get(NPC_ID().length-1)]);
            n.spawnBounds = LOCATION();
            n.walkBounds = LOCATION();
            n.getDef().combatInfo.respawn_ticks = 5;
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

                addPointsToVault(p);
                giveXP(p);
                deplete(p);
            };

        }
    }


    static {

        tasks = new ArrayList<>();
        tasks.add(new ChickenTask());
        tasks.add(new RockCrabTask());
        tasks.add(new MossGiantTask());
        tasks.add(new BloodveldTask());
        tasks.add(new GargoyleTask());

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

        p.deathStartListener = (e, k, h) -> {
            p.getMovement().teleport(2615, 3853);
            p.face(Direction.WEST);
            p.lock();
            p.dialogue(
                    new NPCDialogue(new NPC(2713), "Hah, I didn't realise we asked for amateur hour!"),
                    new NPCDialogue(new NPC(2713), "Your task has been cleared, and you lost your banked points.")
            );
            p.overworldPointsVault = 0;
        };
    }
    static void onExit(Player p, boolean logout) {
        p.attackNpcListener = null;
        p.attackPlayerListener = (p1, p2, msg) -> false;
    }

    static CombatTask getClassForTask(TASK_MONSTER tm) {
        switch (tm) {
            case CHICKEN: return tasks.get(ChickenTask.ID);
            case ROCK_CRAB: return tasks.get(RockCrabTask.ID);
            case MOSS_GIANTS: return tasks.get(MossGiantTask.ID);
            case BLOODVELD: return tasks.get(BloodveldTask.ID);
            case GARGOYLE: return tasks.get(GargoyleTask.ID);

            default: return null;
        }
    }

    public enum TASK_MONSTER {
        NONE("None", -1),
        CHICKEN("Chickens", 1),
        ROCK_CRAB("Rock Crabs", 1),
        MOSS_GIANTS("Moss Giants", 20),
        BLOODVELD("Bloodvelds", 50),
        GARGOYLE("Gargoyles", 75),
        ABYSSAL_DEMON("Abyssal Demons", 85),
        MINI_CERB("Mini Cerberus", 91);

        public final String name;
        final int slayUnlock;
        TASK_MONSTER(String Name, int SlayerLevelUnlock) {
            this.name = Name;
            this.slayUnlock = SlayerLevelUnlock;
        }

        public void assign(Player p, int diff) {
            switch (this) {
                case CHICKEN -> new ChickenTask(p, diff);
                case ROCK_CRAB -> new RockCrabTask(p, diff);
                case MOSS_GIANTS -> new MossGiantTask(p, diff);
                case BLOODVELD -> new BloodveldTask(p, diff);
                case GARGOYLE -> new GargoyleTask(p, diff);
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
        int slaylvl = p.getStats().get(StatType.Slayer).fixedLevel;

        ArrayList<TASK_MONSTER> holder = new ArrayList<>();
        for (TASK_MONSTER tm : TASK_MONSTER.values()) {
            if (tm == TASK_MONSTER.NONE) continue;

            if (slaylvl >= tm.slayUnlock)
                holder.add(tm);
        }
        return holder.get(Random.get(holder.size()-1));
    }

    private class PointCalculator {
        public static int getFinalPoints(int hp, double defMult, int slayerReq) {
            // Core Formula
            double baseToughness = hp * defMult;
            double levelPremium = Math.pow(slayerReq, 2) * 0.5;

            // Apply Difficulty Modifier
            double total = (baseToughness + levelPremium) * defMult;

            return (int) Math.round(total);
        }
    }

}
