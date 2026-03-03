package io.dm.model.inter.actions;

import io.dm.model.entity.player.Player;
import io.dm.model.inter.InterfaceAction;
import io.dm.model.map.object.GameObject;

public interface OnObjectAction extends InterfaceAction {

    void handleOnObject(Player player, int slot, int itemId, GameObject obj);

}