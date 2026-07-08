package io.dm.model.entity.shared.masks;

import io.dm.api.buffer.OutBuffer;
import io.dm.model.entity.shared.UpdateMask;

public class EntityDirectionUpdate extends UpdateMask {

    public int direction = -1;
    private boolean update;
    private int stage;

    /**
     * Logical face-entity target, always current — unlike {@link #direction}, which the legacy 184
     * encoder deliberately lags by up to two cycles on a delayed {@link #remove} (via {@link #stage})
     * to satisfy an old client quirk. RSProt's extended info has no such quirk, so the per-tick bridge
     * to {@code PlayerAvatarExtendedInfo.setFacePathingEntity} reads this instead of {@link #direction}.
     */
    private int resolvedTarget = -1;

    private boolean resolvedDirty;

    public void set(int direction, boolean temporary) {
        this.direction = direction;
        this.stage = (temporary ? 1 : 0);
        this.update = true;
        setSent(false);
        resolvedTarget = direction;
        resolvedDirty = true;
    }

    public void remove(boolean delay) {
        if(delay) {
            /**
             * Delay Remove
             */
            stage = 1;
            setSent(true);
            resolvedTarget = -1;
            resolvedDirty = true;
        } else {
            /**
             * Remove
             */
            set(-1, false);
        }
    }

    public int getResolvedTarget() {
        return resolvedTarget;
    }

    /** Returns true (once) if the logical face target changed since the last call. */
    public boolean consumeResolvedUpdate() {
        if(!resolvedDirty)
            return false;
        resolvedDirty = false;
        return true;
    }

    @Override
    public void reset() {
        update = false;
        if(stage == 1) {
            /**
             * Queue remove
             */
            stage = 2;
            setSent(true);
        } else if(stage == 2) {
            /**
             * Remove
             */
            remove(false);
        }
    }

    @Override
    public boolean hasUpdate(boolean added) {
        return update || (added && direction != -1);
    }

    @Override
    public void send(OutBuffer out, boolean playerUpdate) {
        if(playerUpdate)
            out.addLEShortA(direction);
        else
            out.addShortA(direction);
    }

    @Override
    public int get(boolean playerUpdate) {
        return playerUpdate ? 64 : 2;
    }

}
