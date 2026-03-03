package io.dm.model.entity.shared.masks;

import io.dm.api.buffer.OutBuffer;
import io.dm.model.entity.shared.UpdateMask;

public class TeleportModeUpdate extends UpdateMask {

    private boolean update;

    private int mode;

    public void set(int mode) {
        this.mode = mode;
        this.update = true;
    }

    @Override
    public void reset() {
        update = false;
    }

    @Override
    public boolean hasUpdate(boolean added) {
        return update || added;
    }

    @Override
    public void send(OutBuffer out, boolean playerUpdate) {
        out.addByte(mode);
    }

    @Override
    public int get(boolean playerUpdate) {
        return 512;
    }

}
