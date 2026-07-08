package io.dm.model.activities.donatorzone;

import io.dm.model.activities.pvminstances.InstanceDialogue;
import io.dm.model.map.object.actions.ObjectAction;

public class BossInstancePortal {

    static {
        ObjectAction.register(4407, "use", (player, obj) -> InstanceDialogue.open(player));
    }

}
