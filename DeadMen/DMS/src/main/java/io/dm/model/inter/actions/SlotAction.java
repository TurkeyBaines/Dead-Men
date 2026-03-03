package io.dm.model.inter.actions;

import io.dm.model.entity.player.Player;
import io.dm.model.inter.InterfaceAction;

public interface SlotAction extends InterfaceAction {

    void handle(Player player, int slot);

    @Override
    default void handleClick(Player player, int option, int slot, int itemId) {
        handle(player, slot);
    }

}
