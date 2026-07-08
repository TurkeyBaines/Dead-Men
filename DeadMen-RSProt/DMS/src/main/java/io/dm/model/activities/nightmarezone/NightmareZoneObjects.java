package io.dm.model.activities.nightmarezone;

import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceType;
import io.dm.model.map.object.actions.ObjectAction;

public class NightmareZoneObjects {

    static {
        /**
         * Rewards chest
         */
        ObjectAction.register(26273, "search", (player, obj) -> {
            player.openInterface(InterfaceType.MAIN, Interface.NIGHTMARE_ZONE_REWARDS);
        });
    }
}
