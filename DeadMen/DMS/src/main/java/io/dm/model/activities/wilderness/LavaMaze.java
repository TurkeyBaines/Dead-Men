package io.dm.model.activities.wilderness;

import io.dm.model.map.object.actions.ObjectAction;
import io.dm.model.map.object.actions.impl.Ladder;

public class LavaMaze {
    static {
        ObjectAction.register(18989, 3069, 3856, 0, "climb-down", (player, obj) -> Ladder.climb(player, 3016, 10249, 0, false, true, false));
        ObjectAction.register(18990, 3017, 10249, 0, "climb-up", (player, obj) -> player.getMovement().teleport(3069, 3857, 0));
    }
}
