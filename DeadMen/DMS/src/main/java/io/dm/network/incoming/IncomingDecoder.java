package io.dm.network.incoming;

import io.dm.Server;
import io.dm.api.buffer.InBuffer;
import io.dm.api.netty.Message;
import io.dm.api.netty.MessageDecoder;
import io.dm.api.utils.ISAACCipher;
import io.dm.model.entity.player.Player;

public class IncomingDecoder extends MessageDecoder<Player> {

    private long timeoutAt = Long.MAX_VALUE;

    public IncomingDecoder(ISAACCipher cipher) {
        super(cipher, true);
    }

    @Override
    protected void add(Message message) {
        if(timeoutAt != -1)
            timeoutAt = Server.getEnd(50);
        if(Incoming.IGNORED[message.opcode]) {
            message.buffer = null;
            return;
        }
        super.add(message);
    }

    @Override
    protected void handle(Player player, InBuffer in, int opcode) {
        Incoming handler = Incoming.HANDLERS[opcode];
        if(handler == null) {
            if(player.debug)
                player.sendFilteredMessage("Unhandled incoming opcode: " + opcode);
            return;
        }
        handler.handle(player, in, opcode);
    }

    @Override
    protected int getSize(int opcode) {
        if(opcode < 0 || opcode >= Incoming.SIZES.length)
            return UNHANDLED;
        return Incoming.SIZES[opcode];
    }

    public boolean timeout() {
        if(timeoutAt == -1)
            return true;
        if(!Server.isPast(timeoutAt))
            return false;
        timeoutAt = -1;
        return true;
    }

}