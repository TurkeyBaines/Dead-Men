package io.dm.model.item.actions.impl.scrolls;


import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceType;
import io.dm.model.item.actions.ItemAction;
import io.dm.model.map.Bounds;

public class ImplingScroll {

    public static final int IMPLING_SCROLL = 11273;
    private static final Bounds TOGGLE_BOUNDS = new Bounds(2561, 4289, 2622, 4350, -1);

    static {
        ItemAction.registerInventory(IMPLING_SCROLL, "toggle-view", (player, item) -> {
            if (!player.getPosition().inBounds(TOGGLE_BOUNDS)) {
                player.sendFilteredMessage("You can only use this in the Puro Puro Maze.");
                return;
            }
            if (player.isVisibleInterface(Interface.IMPLING_SCROLL))
                player.closeInterface(InterfaceType.PRIMARY_OVERLAY);
            else
                player.openInterface(InterfaceType.PRIMARY_OVERLAY, Interface.IMPLING_SCROLL);
        });
    }
}
