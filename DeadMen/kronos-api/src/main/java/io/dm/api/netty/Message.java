package io.dm.api.netty;

import io.dm.api.buffer.InBuffer;

public class Message {

    public final int opcode;

    public InBuffer buffer;

    public Message(int opcode, byte[] payload) {
        this.opcode = opcode;
        this.buffer = new InBuffer(payload);
    }

}