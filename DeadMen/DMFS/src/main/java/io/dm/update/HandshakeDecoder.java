package io.dm.update;

import io.netty.channel.Channel;
import io.dm.api.buffer.InBuffer;
import io.dm.api.buffer.OutBuffer;
import io.dm.api.netty.MessageDecoder;
import io.dm.api.protocol.Response;

public class HandshakeDecoder extends MessageDecoder<Channel> {

    public static int REVISION = 226;

    public HandshakeDecoder() {
        super(null, false);
    }

    @Override
    protected void handle(Channel channel, InBuffer in, int opcode) {
        int clientBuild = in.readInt();
        if (clientBuild == REVISION) {
            channel.writeAndFlush(new OutBuffer(REVISION).addByte(0).toBuffer());
            channel.pipeline().replace("decoder", "decoder", new Js5Decoder());
            return;
        }
        Response.GAME_UPDATED.send(channel);
    }

    @Override
    protected int getSize(int opcode) {
        switch (opcode) {
            case 15: {
                return 4;
            }
        }
        return -3;
    }
}

