package io.dm.deadman.areas.overworld.combat;

import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.NPCDialogue;
import io.dm.model.map.Position;
import io.dm.model.map.Tile;
import io.dm.model.map.object.GameObject;
import io.dm.model.map.object.actions.ObjectAction;

public class TaskBoard {

    private static GameObject taskboard;

    static {

        //taskboard = Tile.getObject(32655, 2612, 3858, 0);

        //ObjectAction.register(taskboard, "Read", (p, o) -> displayBoard(p));

    }

    private static void displayBoard(Player p) {
        if (CombatTask.hasTask(p)) {
            showTaskCancellation(p);
            return;
        }


    }

    private static void showTaskCancellation(Player p) {
        p.dialogue(
        );
    }

}
