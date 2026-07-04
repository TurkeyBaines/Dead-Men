package io.dm.deadman.content.areas.citadel;

import io.dm.cache.NpcID;
import io.dm.deadman.Deadman;
import io.dm.deadman.tournament.stages.Final;
import io.dm.model.World;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.npc.NPCAction;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.NPCDialogue;
import io.dm.model.map.Bounds;
import io.dm.model.map.Direction;
import io.dm.model.shop.ShopManager;

import static io.dm.deadman.content.areas.citadel.CitadelShop.*;

public class CitadelNPCs {

    public static void register() {
        registerDeath();
        registerAnythingShop();
        registerCitadelShop();
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
            ShopManager.openIfExists(p, "Sir Sell A Bit's Emporium");
        });
    }

    private static void registerCitadelShop() {

        shopkeep = new NPC(15103).spawn(2961, 3335, 0, Direction.NORTH, 0);

        final int[] SHOPKEEPER_IDS = { 15103, 15108, 15105, 15106 };

        for (int i : SHOPKEEPER_IDS) {
            NPCAction.register(i, "Tier 1 Shop", (p, n) -> openShop(p, "Tier 1 Shop"));
            NPCAction.register(i, "Tier 2 Shop", (p, n) -> openShop(p, "Tier 2 Shop"));
            NPCAction.register(i, "Tier 3 Shop", (p, n) -> openShop(p, "Tier 3 Shop"));
            NPCAction.register(i, "Tier 4 Shop", (p, n) -> openShop(p, "Tier 4 Shop"));
        }
    }

    private static boolean isFinal() {
        return Deadman.getStage() instanceof Final;
    }

    private static void openShop(Player p, String name) {
        if (isFinal()) {
            p.dialogue(new NPCDialogue(shopkeep, "The shop is disabled during the Finals, please wait for the next tournament"));
            return;
        }

        ShopManager.openIfExists(p, name);
    }
}
