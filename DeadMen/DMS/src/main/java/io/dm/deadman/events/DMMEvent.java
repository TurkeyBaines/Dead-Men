package io.dm.deadman.events;

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
        return null; //TODO -- ADD NONE NULL!!!
    }
}
