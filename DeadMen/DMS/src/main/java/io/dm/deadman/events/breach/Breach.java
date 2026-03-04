package io.dm.deadman.events.breach;

import io.dm.deadman.events.DMMEvent;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.shared.StepType;
import io.dm.model.map.Bounds;
import io.dm.model.map.Position;

import java.util.ArrayList;
import java.util.List;

public class Breach extends DMMEvent {

    private List<NPC> npcs;
    private int npcCount;
    private int maxCount;
    private long delay;
    private long lastTime;
    private long nextTime;

    @Override
    public void spawn() {
        npcs = new ArrayList<>();

        npcCount = 0;
        maxCount = 5;
        delay = 60000;

        lastTime = System.currentTimeMillis();
        nextTime = lastTime + delay;

        NPC n = new NPC(6506).spawn(bounds().randomPosition());
        npcs.add(n);
    }

    @Override
    public void update() {
        if (npcCount < maxCount)
            if (System.currentTimeMillis() > nextTime) {
                NPC n = new NPC(6506).spawn(bounds().randomPosition());
                npcs.add(n);
                lastTime = System.currentTimeMillis();
                nextTime = lastTime + delay;
                npcCount++;
            }

    }

    @Override
    public void despawn() {
        for (NPC n : npcs) {
            if (n != null) {
                n.remove();
            }
        }
        npcs = null;
    }

    @Override
    public Bounds bounds() {
        return new Bounds(new Position(0, 0, 0), 0);
    }
}
