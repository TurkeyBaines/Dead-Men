package io.dm.network.incoming.handlers;

import io.dm.api.buffer.InBuffer;
import io.dm.model.World;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceType;
import io.dm.network.incoming.Incoming;
import io.dm.utility.IdHolder;

/**
 * Fired by the client when double-clicking a location on the world map.
 * Only ever granted to admins/owners (see MainFrame's world map access mask) —
 * still gated here since packets are never trusted client-side.
 */
@IdHolder(ids = {7})
public class TeleportHandler implements Incoming {

    @Override
    public void handle(Player player, InBuffer in, int opcode) {
        int z = in.readByteS();
        int unknown = in.readLEInt();
        int y = in.readShort();
        int x = in.readLEShortA();

        if (!(player.isAdmin() || World.isDev()) || !player.isVisibleInterface(Interface.WORLD_MAP))
            return;

        player.getMovement().teleport(x, y, z);
        player.sendFilteredMessage("<col=cc0000>::tele: " + x + "," + y + "," + z);
        player.closeInterface(InterfaceType.WORLD_MAP);
    }

}
