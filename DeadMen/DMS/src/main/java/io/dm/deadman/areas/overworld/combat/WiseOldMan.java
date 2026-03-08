package io.dm.deadman.areas.overworld.combat;

import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.npc.NPCAction;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.NPCDialogue;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.utils.Option;
import io.dm.model.map.Bounds;

public class WiseOldMan {

    private static NPC wom;

    static {
        wom = new NPC(2713);
        wom.getDef().options[0] = "Talk-to";
        NPCAction.register(wom, "Talk-to", (p, n) -> p.dialogue(new NPCDialogue(wom, "Beautiful day isn't it!")));
        wom.getDef().options[1] = "Check-task";
        NPCAction.register(wom, "Talk-to", (p, n) -> p.dialogue(new NPCDialogue(wom, CombatTask.hasTask(p) ? "You don't have a task at the moment" : "You still need to kill " + p.overworldTaskRemaining + " x " + p.overworldTaskMonster.name)));
        wom.getDef().options[2] = "Quick-task";
        NPCAction.register(wom, "Quick-task", (p, n) -> {
            if (CombatTask.hasTask(p)) {
                if (p.overworldTaskRemaining > 0) {
                    showExistingTask(p);
                } else {
                    showDifficultyMenu(p);
                }
                return;
            }

            p.overworldTaskMonster = CombatTask.getNewTask(p);
            showDifficultyMenu(p);
        });
        wom.getDef().options[3] = "Cancel-task";

        wom.walkBounds = new Bounds(2610, 3852, 2614, 3857, 0);

        wom.spawn(2612, 3855, 0);


    }

    private static void showDifficultyMenu(Player p) {
        p.dialogue(
                new OptionsDialogue(
                        "You're task is to kill " + p.overworldTaskMonster.name + ". Select a difficulty",
                        new Option("Easy", () -> p.overworldTaskMonster.assign(p, 0)),
                        new Option("Medium", () -> p.overworldTaskMonster.assign(p, 1)),
                        new Option("Hard", () -> p.overworldTaskMonster.assign(p, 2))
                )
        );
    }

    private static void showExistingTask(Player p) {
        p.dialogue(new NPCDialogue(wom, "You already have a task to kill " + p.overworldTaskRemaining + " x " + p.overworldTaskMonster.name));
    }

}
