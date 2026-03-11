package io.dm.deadman.tournament.finals;

import io.dm.deadman.tournament.FinalEvent;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.shared.listeners.DeathListener;
import io.dm.model.inter.utils.Config;
import io.dm.model.map.Bounds;
import io.dm.model.map.Position;

import java.sql.SQLOutput;

public class FFA extends FinalEvent {

    Bounds lobby = new Bounds(new int[][]{
            {2965, 3341},
            {2965, 3346},
            {2976, 3346},
            {2976, 3341}
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
            System.out.println("We need to spawn the ENDING!");
            spawnedEnding = true;
            end();
        } else {
            System.out.println();
            System.out.println("WE'RE WAITING FOR THE END");
            System.out.println("Size: " + players.size());
            System.out.println("spawnedEnding: " + spawnedEnding);
            System.out.println();
        }
    }
}
