package io.dm.model.entity.npc.actions.edgeville;

import io.dm.model.entity.shared.listeners.SpawnListener;

public class MeleeShop {

    private final static int SHOPKEEPER = 2153;

    static {
        SpawnListener.register(SHOPKEEPER, npc -> npc.skipReachCheck = p -> p.equals(3078, 3511));
    }
}
