package io.dm.deadman.tournament.finals;

import io.dm.deadman.tournament.FinalEvent;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.shared.listeners.DeathListener;
import io.dm.model.inter.utils.Config;
import io.dm.model.map.Bounds;
import io.dm.model.map.Position;

public class FFA extends FinalEvent {

    Bounds lobby = new Bounds(new int[][]{

    }, 0);

    @Override
    public void init() {
        bounds = new Bounds(2965, 3341, 2976, 3346, 0);

        players.forEach(p -> {
            Position pos = bounds.randomPosition();
            p.getMovement().teleport(pos);

            p.sendMessage("Welcome to the Finals! This event is a Free For All (MULTI).");
            p.sendMessage("The last player alive is the Winner.");
            p.sendMessage("Good Luck!");
        });

    }

    @Override
    public void update() {
        if (players.size() == 1 && !spawnedEnding) {
            spawnedEnding = true;
            end();
        }
    }
}
