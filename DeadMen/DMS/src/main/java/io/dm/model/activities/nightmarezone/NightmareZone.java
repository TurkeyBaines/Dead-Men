package io.dm.model.activities.nightmarezone;

import io.dm.model.inter.InterfaceType;
import io.dm.model.map.Bounds;
import io.dm.model.map.MapListener;

public class NightmareZone {

    private static Bounds PREP_AREA = new Bounds(3118, 3481, 3127, 3487, 0);

    static {
        MapListener.registerBounds(PREP_AREA).onEnter(player -> {
            player.openInterface(InterfaceType.PRIMARY_OVERLAY, 207);
        })
        .onExit((player, logout) -> {
            player.closeInterface(InterfaceType.PRIMARY_OVERLAY);
        });
    }

}
