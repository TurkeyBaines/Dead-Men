package io.dm.deadman.content.areas.overworld.combat;

import io.dm.model.entity.player.Player;
import io.dm.model.map.object.GameObject;

public class TaskBoard {

    private static GameObject taskboard;

    static {

        //taskboard = Tile.getObject(32655, 2612, 3858, 0);

        //ObjectAction.register(taskboard, "Read", (p, o) -> displayBoard(p));Not
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
