package io.dm.utility;

import io.dm.model.map.Position;
import io.dm.model.map.object.actions.ObjectAction;

public class MovementFixes {

    static {

        // Tree Gnome Village
            // Door
        ObjectAction.register(1967, "Open", (p, o) -> { if (p.getPosition().getY() == 3491) p.getMovement().teleport(p.getPosition().getX(), 3492); else p.getMovement().teleport(p.getPosition().getX(), 3491); });
        ObjectAction.register(1968, "Open", (p, o) -> { if (p.getPosition().getY() == 3491) p.getMovement().teleport(p.getPosition().getX(), 3492); else p.getMovement().teleport(p.getPosition().getX(), 3491); });

            // Bank Stairs
        ObjectAction.register(16675, 2444, 3414, 0, "Climb-up", (p, o) -> { p.getMovement().teleport(2445, 3416); });
        ObjectAction.register(16675, 2445, 3416, 0, "Climb-down", (p, o) -> { p.getMovement().teleport(2444, 3413); });
    }

}
