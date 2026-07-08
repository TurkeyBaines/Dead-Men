package io.dm.model.inter.handlers;

import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceHandler;
import io.dm.model.inter.InterfaceType;
import io.dm.model.inter.actions.SimpleAction;

public class WorldMap {
    static {
        InterfaceHandler.register(Interface.WORLD_MAP, h -> h.actions[37] = (SimpleAction) p -> p.closeInterface(InterfaceType.WORLD_MAP));
    }
}
