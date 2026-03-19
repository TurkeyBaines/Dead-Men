package io.dm.model.inter.handlers;

import io.dm.cache.Color;
import io.dm.deadman.Deadman;
import io.dm.model.World;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.InterfaceType;

public class GroupOverlay {

    public static void send(Player p) {

        if (Deadman.getConfig().TEAM_SIZE_MAX.asInt == 1)
            return;

        int interId = 256;

        p.openInterface(InterfaceType.PRIMARY_OVERLAY, interId);
        World.startEvent(e -> {
            while(Deadman.getCitadel().contains(p)) {
                if (p.hasGroup()) {
                    if (p.getGroup() == null) {
                        p.groupID = null;
                        continue;
                    }

                    p.getPacketSender().sendString(interId, 9, Color.GREEN.wrap(p.getGroup().getName()));
                    for (int i = 0; i < Deadman.getConfig().TEAM_SIZE_MAX.asInt; i++) {
                        String name = p.getGroup().getMemberStrings().get(i) != null ? p.getGroup().getMemberStrings().get(i) : "";
                        p.getPacketSender().sendString(interId, 16+i, "Player " + (i+1) + ":");
                        p.getPacketSender().sendString(interId, 11+i, Color.CYAN.wrap(name));
                    }
                } else {
                    p.getPacketSender().sendString(interId, 9, "No Team");
                    for (int i = 0; i < Deadman.getConfig().TEAM_SIZE_MAX.asInt; i++) {
                        p.getPacketSender().sendString(interId, 16+i, "Player " + (i+1) + ":");
                        p.getPacketSender().sendString(interId, 11+i, "");
                    }
                }
                e.delay(1);
            }
        });

    }

}
