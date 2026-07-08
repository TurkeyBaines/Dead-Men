package io.dm.model.inter.handlers;

import io.dm.model.entity.player.Player;
import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceType;

public class CollectionBox {

    public static void open(Player player) {
        player.openInterface(InterfaceType.MAIN, Interface.COLLECTION_BOX);
        //todo
    }

}
