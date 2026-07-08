package io.dm.network.incoming.handlers;

import io.dm.api.buffer.InBuffer;
import io.dm.model.entity.player.Player;
import io.dm.network.incoming.Incoming;
import io.dm.utility.IdHolder;

@IdHolder(ids = {57})
public class CloseInterfaceHandler implements Incoming {

    @Override
    public void handle(Player player, InBuffer in, int opcode) {
        player.closeInterfaces();
    }

}