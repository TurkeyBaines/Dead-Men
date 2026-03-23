package io.dm.deadman.content.areas.citadel;

import io.dm.cache.NpcID;
import io.dm.model.World;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.npc.NPCAction;
import io.dm.model.map.Bounds;
import io.dm.model.map.Direction;
import io.dm.model.shop.ShopManager;

public class CitadelNPCs {

    public static void register() {
        registerDeath();
        registerAnythingShop();
    }

    private static void registerDeath() {
        new NPC(NpcID.DEATH_CITADEL).spawn(2971, 3338, 1, Direction.EAST, 0);
        NPCAction.register(15100, "Escape", (p, n) -> {
            p.startEvent(e -> {
                p.lock();
                p.animate(2820);
                e.delay(5);
                p.getMovement().teleport(World.HOME);
                e.delay(2);
                p.animate(-1);
                e.delay(2);
                p.unlock();
            });
        });
    }

    private static void registerAnythingShop() {
        NPC npc = new NPC(NpcID.SIR_SELL_A_BIT);
        npc.walkBounds = new Bounds(2957, 3335, 2963, 3342);
        npc.spawn(2961, 3339, 0);

        NPCAction.register(NpcID.SIR_SELL_A_BIT, "Trade", (p, n) -> {
            ShopManager.openIfExists(p, "472x281s-3k8d-10z8-99m2-206d6942cff8");
        });
    }
}
