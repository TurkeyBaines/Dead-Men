package io.dm.model.skills.hunter.traps.impl;

import io.dm.model.entity.player.Player;
import io.dm.model.map.object.GameObject;
import io.dm.model.skills.hunter.Hunter;
import io.dm.model.skills.hunter.traps.TrapType;

public class BoxTrap implements TrapType {

    public static final TrapType INSTANCE = new BoxTrap();

    static {
        Hunter.registerTrap(INSTANCE, true);
    }

    private BoxTrap() {

    }

    @Override
    public int getItemId() {
        return 10008;
    }

    @Override
    public int getLevelReq() {
        return 27;
    }

    @Override
    public int getActiveObjectId() {
        return 9380;
    }

    @Override
    public int getFailedObjectId() {
        return 9385;
    }

    @Override
    public int getPlaceAnimation() {
        return 5208;
    }

    @Override
    public int getDismantleAnimation() {
        return 5212;
    }

    @Override
    public int[] getSuccessObjects() {
        return new int[]{9384};
    }

    @Override
    public void onPlace(Player player, GameObject object) {

    }

    @Override
    public void onRemove(Player player, GameObject object) {

    }

}
