package io.dm.model.inter.actions;

import io.dm.model.entity.player.Player;
import io.dm.model.inter.InterfaceAction;

public interface DefaultAction extends InterfaceAction {

    void handleClick(Player player, int option, int slot, int itemId);

}