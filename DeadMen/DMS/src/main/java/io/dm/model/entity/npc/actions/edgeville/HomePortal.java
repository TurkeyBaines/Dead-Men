package io.dm.model.entity.npc.actions.edgeville;

import io.dm.model.entity.npc.NPCAction;
import io.dm.model.entity.shared.listeners.SpawnListener;
import io.dm.model.item.actions.impl.ItemSet;

public class HomePortal {

    static {

        for(int trader : new int[]{2149, 2148}) {
            SpawnListener.register(trader, npc -> npc.startEvent(event -> {
                while (true) {
                    npc.forceText("Come check out the trading post!");
                    event.delay(150);
                }
            }));
            NPCAction.register(trader, "sets", (player, npc) -> ItemSet.open(player));
            NPCAction.register(trader, "exchange", ((player, npc) -> {
                if (player.getGameMode().isIronMan()) {
                    player.sendMessage("Your gamemode prevents you from accessing the trading post!");
                    return;
                }
                player.getTradePost().openViewOffers();
            }));
        }

        SpawnListener.register(4159, npc -> npc.startEvent(event -> {
            while (true) {
                npc.forceText("Use the portal to teleport around the world!");
                event.delay(100);
            }
        }));

//        ObjectAction.register(ObjectID.KRONOS_TELEPORTER, "teleport", (player, npc) -> teleports.open(player));
//        ObjectAction.register(ObjectID.KRONOS_TELEPORTER, "previous teleport", (player, npc) -> teleports.previous(player));
    }
}
