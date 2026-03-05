package io.dm.deadman.areas;

import io.dm.deadman.guard.DMMGuard;
import io.dm.model.World;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.npc.NPCAction;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.player.PlayerAction;
import io.dm.model.entity.shared.LockType;
import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceType;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.utils.Config;
import io.dm.model.inter.utils.Option;
import io.dm.model.item.Item;
import io.dm.model.item.ItemContainer;
import io.dm.model.map.Bounds;
import io.dm.model.map.Direction;
import io.dm.model.map.MapListener;
import io.dm.model.map.Position;
import io.dm.model.map.object.GameObject;
import io.dm.model.map.object.actions.ObjectAction;
import io.dm.model.stat.StatList;
import io.dm.model.stat.StatType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Citadel {

    private static List<Player> players;
    private static HashMap<Player, DMMGuard> guards;

    private static Bounds bounds = new Bounds(new int[][] {
            {2957, 3356},
            {2987, 3356},
            {3003, 3342},
            {2987, 3323},
            {2959, 3322},
            {2950, 3335},
            {2950, 3343}
    }, 0);

    private static Bounds spawnPoints = new Bounds(new int[][] {
            { 2963, 3349 },
            { 2977, 3349 },
            { 2979, 3347 },
            { 2981, 3347 },
            { 2981, 3344 },
            { 2982, 3343 },
            { 2982, 3339 },
            { 2981, 3338 },
            { 2981, 3337 },
            { 2965, 3337 },
            { 2965, 3342 },
            { 2963, 3344 }
    }, 0);

    public Citadel() {
        players = new ArrayList<>();
        guards = new HashMap<>();

        ObjectAction.register(10251, 1, ((player, obj) -> {
            player.startEvent(e -> {
                player.lock(LockType.FULL);
                player.face(Direction.EAST);
                swapOverworld(player);
                if (!player.getPosition().isWithinDistance(new Position(2963, 3346, 0)))
                    player.getMovement().teleport(2963, 3346, 0);
                else
                    player.getMovement().teleport(2540, 3872, 0);
                player.unlock();
            });
        }));

        ObjectAction.register(11005, "Pass-through", ((player, obj) ->  {
            player.startEvent(e -> {
                player.lock();
                player.animate(535);
                e.delay(1);
                if (player.getPosition().getY() < 3353)
                    player.getMovement().teleport(player.getPosition().getX(), 3354, 0);
                else
                    player.getMovement().teleport(player.getPosition().getX(), 3352, 0);
                player.unlock();
            });
        }));

        NPC death = new NPC(5567);
        death.spawn(2971, 3338, 1, Direction.EAST, 0);
        NPCAction.register(5567, "Escape", (p, n) -> {
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

        MapListener.registerBounds(bounds)
                .onEnter(Citadel::entered)
                .onExit(Citadel::exited);
        System.out.println("Registered Bounds");

    }


    private static void entered(Player player) {
        System.out.println("Player Entered!");
        players.add(player);
        player.attackPlayerListener = Citadel::allowAttack;
        player.attackNpcListener = Citadel::allowNPCAttack;
        player.closeInterface(InterfaceType.WILDERNESS_OVERLAY);
        Config.IN_PVP_AREA.set(player, 0);
        player.setAction(1, null);

//        if (player.getSkullTimer().skulled()) {
//            NPC n = new DMMGuard();
//            n.setCombat(new DMMGuardCombat());
//            n.spawn(player.getPosition());
//        }
    }

    private static void exited(Player player, boolean logout) {
        if (logout) return;
        players.remove(player);
        System.out.println("Player Exited!");
        player.attackPlayerListener = Citadel::allowAttack;
        player.attackNpcListener = Citadel :: allowNPCAttack;
        player.closeInterface(InterfaceType.WILDERNESS_OVERLAY);
        Config.IN_PVP_AREA.set(player, 1);
        player.setAction(1, PlayerAction.ATTACK);

        if (guards.containsKey(player)) {
            guards.get(player).remove();
        }
    }

    public boolean contains(Player player) {
        return bounds.inBounds(player.getPosition());
    }

    private static boolean allowAttack(Player player, Player pTarget, boolean message) {
        if (player.getPosition().inBounds(bounds)) {
            return false;
        }
        return true;
    }
    private static boolean allowNPCAttack(Player player, NPC npc, boolean message) {
        if (player.getPosition().inBounds(bounds)) {
            return false;
        }
        return true;
    }

    private static void swapOverworld(Player p) {

        // == Stats ==
        int[][] ostats = p.skillHolder;
        int[][] holder = new int[StatType.values().length][2];
        for (int i = 0; i < StatType.values().length; i++) {
            holder[i][0] = p.getStats().get(StatType.get(i)).fixedLevel;
            holder[i][1] = (int) p.getStats().get(StatType.get(i)).experience;
            p.getStats().set(StatType.get(i), ostats[i][0], ostats[i][1]);
        }
        p.skillHolder = holder;

        // == Inventory ==
        ItemContainer holder2 = new ItemContainer();
        holder2.init(28, false);
        for (Item i : p.getInventory().getItems()) {
            if (i == null) continue;
            holder2.add(i);
            p.getInventory().remove(i);
        }
        if (p.inventoryHolder != null && p.inventoryHolder.getItems() != null)
            for (Item i : p.inventoryHolder.getItems()) {
                if (i == null) continue;

                p.getInventory().add(i);
            }
        p.inventoryHolder = holder2;

        // == Equipment ==
        Item[] equipment = new Item[14];
        for (int i = 0; i < 14; i++) {
            if (i == 6 || i == 8 || i == 11) continue;
            equipment[i] = p.getEquipment().get(i);
            if (p.equipmentHolder != null && p.equipmentHolder[i] != null)
                p.getEquipment().set(i, p.equipmentHolder[i]);
            else
                p.getEquipment().set(i, null);
        }
        p.equipmentHolder = equipment;

    }

    public int count() {
        return players.size();
    }

    public Position getRandomSpawnPoint() {
        return spawnPoints.randomPosition();
    }

    public void sendTeleportMenu(Player p, int page) {
        switch(page) {
            case 0:
                p.dialogue(new OptionsDialogue("Select a location... [1/2]",
                        new Option("Varrock", () -> p.getMovement().teleport(3182, 3440)),
                        new Option("Falador", () -> p.getMovement().teleport(3016, 3357)),
                        new Option("Lumbridge", () -> p.getMovement().teleport(0, 0)),
                        new Option("Seers Village", () -> p.getMovement().teleport(0, 0)),
                        new Option("> Next >", () -> sendTeleportMenu(p, 1))));
                break;

            case 1:
                p.dialogue(new OptionsDialogue("Select a location... [2/2]",
                        new Option("Ardougne", () -> p.getMovement().teleport(0, 0)),
                        new Option("Rellekka", () -> p.getMovement().teleport(0, 0)),
                        new Option("Edgeville", () -> p.getMovement().teleport(0, 0)),
                        new Option("Canifis", () -> p.getMovement().teleport(0, 0)),
                        new Option("< Back <", () -> sendTeleportMenu(p, 0))));
                break;
        }
    }
}
