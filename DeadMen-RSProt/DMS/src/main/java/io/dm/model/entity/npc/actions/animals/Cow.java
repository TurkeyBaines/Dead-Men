package io.dm.model.entity.npc.actions.animals;

import io.dm.api.utils.ArrayUtils;
import io.dm.api.utils.Random;
import io.dm.model.entity.shared.listeners.SpawnListener;

public class Cow {

    static {
        SpawnListener.register(ArrayUtils.of("cow"), npc -> {
            npc.addEvent(e -> {
                while(true) {
                    e.delay(Random.get(30, 100));
                    npc.forceText("Moo");
                    npc.publicSound( 3044, 7, 0);
                }
            });
        });
    }
}