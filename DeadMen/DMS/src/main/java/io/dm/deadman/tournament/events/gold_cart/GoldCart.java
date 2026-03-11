package io.dm.deadman.tournament.events.gold_cart;

import io.dm.deadman.tournament.events.DMMEvent;
import io.dm.model.entity.npc.NPC;
import io.dm.model.map.Bounds;
import io.dm.model.map.Position;

public class GoldCart extends DMMEvent {

    private NPC goblin;

    @Override
    public void spawn() {
        Position spawnPoint = bounds().randomPosition();

        if (goblin != null) goblin = null;

        goblin = new NPC(5208).spawn(spawnPoint);
    }

    @Override
    public void update() {

    }

    @Override
    public void despawn() {

    }

    @Override
    public Bounds bounds() {
        return null;
    }
}
