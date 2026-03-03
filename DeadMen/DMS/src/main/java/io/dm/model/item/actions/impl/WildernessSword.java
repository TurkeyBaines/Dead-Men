package io.dm.model.item.actions.impl;

import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.item.actions.ItemAction;

public class WildernessSword {

    private static boolean teleport(Player player, Item item) {
        return player.getMovement().startTeleport(e -> {
            player.animate(714);
            player.graphics(111, 92, 0);
            player.publicSound(200);
            e.delay(3);
            player.getMovement().teleport(3359, 3892, 0);
        });
    }

    static {
        ItemAction.registerInventory(13111, "teleport", WildernessSword::teleport);
        ItemAction.registerEquipment(13111, "teleport", WildernessSword::teleport);
    }

}
