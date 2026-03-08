package io.dm.network.incoming.handlers.commands;

import io.dm.Server;
import io.dm.cache.NPCDef;
import io.dm.deadman.Deadman;
import io.dm.deadman.areas.SafeZone;
import io.dm.deadman.areas.overworld.combat.CombatTask;
import io.dm.deadman.areas.overworld.combat.tasks.ChickenTask;
import io.dm.deadman.gas.GasArea;
import io.dm.deadman.mutators.impl.StaticGasMutator;
import io.dm.model.entity.player.Player;
import io.dm.model.map.Bounds;
import io.dm.model.map.Position;

public class TestCommands {

    private long serverticks = 0;
    private GasArea gasArea = null;
    private StaticGasMutator gasMutator = null;

    public void process(Player p, String... args) {
        switch (args[0]) {
            case "print":
                print(p, args);
                break;

            case "gas":
                gas(p, args);
                break;

            case "task":
                task(p, args);
                break;

            case "check":
                check(p, args);
                break;
        }
    }


    private void print(Player p, String... args) {

        switch (args[1]) {

            case "servertick":
                p.sendMessage("Current Tick: " + Server.currentTick());
                p.sendMessage("Time since last check: " + (Server.currentTick() - serverticks));
                serverticks = Server.currentTick();
                break;

        }

    }

    private void gas(Player p, String... args) {
        switch (args[1]) {
            case "new":
                gasArea = new GasArea(new Bounds(
                        p.getPosition().getX()-2,
                        p.getPosition().getY()-2,
                        p.getPosition().getX()+2,
                        p.getPosition().getY()+2,
                        0
                ));
                break;

            case "shrink":
                if (gasArea == null) {
                    p.sendMessage("No gasArea variable initialized. Call 'new' first");
                    break;
                }
                gasArea.shrink();
                break;

            case "expand":
                if (gasArea == null) {
                    p.sendMessage("No gasArea variable initialized. Call 'new' first");
                    break;
                }
                gasArea.expand();
                break;

            case "remove":
                if (gasArea == null) {
                    p.sendMessage("No gasArea variable initialized. Call 'new' first");
                    break;
                }
                gasArea.stop = true;
                gasArea.remove();
                break;

            case "region":
                if (args.length == 3 && args[2].equalsIgnoreCase("remove")) {
                    if (gasMutator == null) {
                        p.sendMessage("No Region Gas detected, variable is null.");
                        return;
                    }
                    gasMutator.clear();
                    gasMutator = null;

                } else {
                    if (gasMutator != null) {
                        p.sendMessage("Existing Region Gas detected, remove existing to continue.");
                        return;
                    }
                    gasMutator = new StaticGasMutator();
                    gasMutator.action();
                }
        }

    }

    private void task(Player p, String... args) {
        System.out.println("args length: " + args.length);
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("print")) {
                CombatTask.TASK_MONSTER taskMonster = p.overworldTaskMonster;
                if (taskMonster == CombatTask.TASK_MONSTER.NONE) {
                    p.sendMessage("NO TASK");
                    return;
                }
                String taskName = taskMonster.name();
                int total = p.overworldTaskTotal;
                int remain = p.overworldTaskRemaining;
                p.sendMessage("Task: " + taskName + " [" + remain + "/" + total + "]");
                return;
            }
        }

        new ChickenTask(p, 1);
    }

    public void check(Player p, String... args) {
        System.out.println("Type: " + args[1]);
        System.out.println("ID: " + args[2]);
        switch (args[1]) {
            case "npc":
                NPCDef def = NPCDef.get(Integer.parseInt(args[2]));
                p.sendMessage("NPC Def for id: " + args[2]);
                p.sendMessage("Name: " + def.name);
                p.sendMessage("Desc Name: " + def.descriptiveName);
                if (def.combatInfo != null) {
                    p.sendMessage("Combat Level: " + def.combatLevel);
                    p.sendMessage("Respawn Ticks: " + def.combatInfo.respawn_ticks);
                    p.sendMessage("Aggression Level: " + def.combatInfo.aggressive_level);
                }
                for (int i = 0; i < def.options.length; i++) {
                    p.sendMessage("Action (" + i + "): " + def.options[i]);
                }
                break;
        }

    }

}
