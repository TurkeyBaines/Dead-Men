package io.dm.network.incoming.handlers;

import io.dm.api.buffer.InBuffer;
import io.dm.model.entity.player.Player;
import io.dm.network.incoming.Incoming;
import io.dm.utility.IdHolder;

@IdHolder(ids = {7})
public class TeleportHandler implements Incoming {

    @Override
    public void handle(Player player, InBuffer in, int opcode) {
        int z = in.readByteS();
        int unknown = in.readLEInt();
        int y = in.readShort();
        int x = in.readLEShortA();

        player.getMovement().teleport(x, y, z);
    }

}
