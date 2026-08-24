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
        ObjectAction.register(16675, "Climb-up", (p, o) -> {
            if (p.getPosition().getY() < 3420)
                p.getMovement().teleport(2445, 3416, 1);
            else
                p.getMovement().teleport(2445, 3433, 1);
        });
        ObjectAction.register(16677, "Climb-down", (p, o) -> {
            if (p.getPosition().getY() < 3420)
                p.getMovement().teleport(2444, 3413, 0);
            else
                p.getMovement().teleport(2445, 3436, 0);
        });
    }

}
