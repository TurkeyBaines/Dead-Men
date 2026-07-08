package io.dm.model.entity.npc.actions.edgeville;

import io.dm.model.entity.shared.listeners.SpawnListener;

public class MagicShop {

    private final static int SHOPKEEPER = 5081;

    static {
        SpawnListener.register(SHOPKEEPER, npc -> npc.skipReachCheck = p -> p.equals(3078, 3508));
    }
}
