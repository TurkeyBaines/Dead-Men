package io.dm.model.map.object.actions.impl;

import io.dm.data.impl.teleports;
import io.dm.model.entity.player.Player;
import io.dm.model.map.object.actions.ObjectAction;

/*
 * @project Kronos
 * @author Patrity - https://github.com/Patrity
 * Created on - 6/16/2020
 */
public class EdgevillePortal {

    static {
        ObjectAction.register(34752, "Use", ((player, obj) -> teleport(player)));
    }

    private static void teleport(Player player) {
        teleports.teleport(player, 3085, 3492, 0);
    }
}
