package io.dm.model.entity.shared.masks;

import io.dm.api.buffer.OutBuffer;
import io.dm.api.filestore.utility.Huffman;
import io.dm.api.utils.StringUtils;
import io.dm.model.entity.shared.UpdateMask;

public class ChatUpdate extends UpdateMask {

    private boolean shadow;

    private int type;

    private int effects;

    private int rankId;

    private byte[] messageData;

    private String message;

    public void set(boolean shadow, int effects, int rankId, int type, String message) {
        this.shadow = shadow;
        this.effects = effects;
        this.rankId = rankId;
        this.type = type;
        message = StringUtils.fixCaps(message);
        this.message = message;
        OutBuffer out = new OutBuffer(255);
        Huffman.encrypt(out, message);
        this.messageData = out.toByteArray();
    }

    public String getMessage() {
        return message;
    }

    public int getEffects() {
        return effects;
    }

    public int getRankId() {
        return rankId;
    }

    public int getType() {
        return type;
    }

    @Override
    public void reset() {
        messageData = null;
        message = null;
    }

    @Override
    public boolean hasUpdate(boolean added) {
        return messageData != null;
    }

    @Override
    public void send(OutBuffer out, boolean playerUpdate) {
        out.addShort(effects);
        out.addByte(rankId);
        out.addByteC(type);
        out.addByteC(messageData.length);
        out.addBytes184(messageData);
    }

    @Override
    public int get(boolean playerUpdate) {
        return 128;
    }

}