package io.dm.model.inter.actions;

import io.dm.model.entity.player.Player;
import io.dm.model.inter.InterfaceAction;

public interface SimpleAction extends InterfaceAction {

    void handle(Player player);

    @Override
    default void handleClick(Player player, int option, int slot, int itemId) {
        handle(player);
    }

}
