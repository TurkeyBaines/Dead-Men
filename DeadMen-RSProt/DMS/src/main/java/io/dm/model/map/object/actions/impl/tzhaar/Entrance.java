package io.dm.model.map.object.actions.impl.tzhaar;

import io.dm.model.entity.player.Player;
import io.dm.model.map.object.actions.ObjectAction;

public class Entrance {

    private static void enter(Player player, int x, int y) {
        player.startEvent(event -> {
            player.lock();
            player.getMovement().teleport(x, y, 0);
            player.unlock();
        });
    }

    static {
        /**
         * Entrance
         */
        ObjectAction.register(11835, "enter", (player, obj) -> enter(player, 2480, 5175));

        /**
         * Exit
         */
        ObjectAction.register(11836, "enter", (player, obj) -> enter(player, 2862, 9572));
    }
}
