package io.dm.deadman.tournament.events;

import io.dm.model.map.Bounds;

public abstract class DMMEvent {

    public long startTime;
    public long lastUpdate;
    public boolean eventFinished = false;

    public abstract void spawn();
    public abstract void update();
    public abstract void despawn();

    public abstract Bounds bounds();

    public long timeSinceLastUpdate() {
        return System.currentTimeMillis() - lastUpdate;
    }

    public long timeSinceStart() {
        return System.currentTimeMillis() - startTime;
    }

    public static DMMEvent getRandom() {
        // Weighted pick across the events that are actually wired up. Kept defensive:
        // Main.onUpdate() immediately calls spawn() on whatever this returns, so it
        // must never be null.
        if (io.dm.api.utils.Random.get(100) < 55) {
            return new io.dm.deadman.tournament.events.koth.KingOfTheHill();
        }
        return new io.dm.deadman.tournament.events.chest.StaticChest();
    }
}
