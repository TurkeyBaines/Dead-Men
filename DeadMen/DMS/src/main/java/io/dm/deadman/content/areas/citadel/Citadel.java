package io.dm.deadman.content.areas.citadel;

import io.dm.cache.Color;
import io.dm.cache.ItemID;
import io.dm.cache.NpcID;
import io.dm.deadman.Deadman;
import io.dm.deadman.content.items.TournamentTicket;
import io.dm.deadman.content.guard.DMMGuard;
import io.dm.deadman.tournament.team.Group;
import io.dm.model.World;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.npc.NPCAction;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.player.PlayerAction;
import io.dm.model.entity.shared.LockType;
import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceType;
import io.dm.model.inter.dialogue.MessageDialogue;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.dialogue.YesNoDialogue;
import io.dm.model.inter.handlers.GroupOverlay;
import io.dm.model.inter.handlers.TournamentInformation;
import io.dm.model.inter.utils.Config;
import io.dm.model.inter.utils.Option;
import io.dm.model.item.Item;
import io.dm.model.item.ItemContainer;
import io.dm.model.map.Bounds;
import io.dm.model.map.Direction;
import io.dm.model.map.MapListener;
import io.dm.model.map.Position;
import io.dm.model.map.object.actions.ObjectAction;
import io.dm.model.shop.ShopManager;
import io.dm.model.skills.magic.SpellBook;
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

        CitadelObjects.register();
        CitadelNPCs.register();

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
        player.openInterface(InterfaceType.WILDERNESS_OVERLAY, Interface.WILDERNESS_OVERLAY);
        player.getPacketSender().setHidden(Interface.WILDERNESS_OVERLAY, 63, false); //hide safe area sprite
        player.getPacketSender().setHidden(Interface.WILDERNESS_OVERLAY, 66, true); //show wilderness level
        Config.IN_PVP_AREA.set(player, 0);
        player.setAction(1, PlayerAction.INVITE);
        GroupOverlay.send(player);
    }

    private static void exited(Player player, boolean logout) {
        if (logout) return;
        players.remove(player);
        System.out.println("Player Exited!");
        player.attackPlayerListener = null;
        player.attackNpcListener = null;
        player.openInterface(InterfaceType.WILDERNESS_OVERLAY, Interface.WILDERNESS_OVERLAY);
        player.getPacketSender().setHidden(Interface.WILDERNESS_OVERLAY, 63, true); //hide safe area sprite
        player.getPacketSender().setHidden(Interface.WILDERNESS_OVERLAY, 66, false); //show wilderness level
        Config.IN_PVP_AREA.set(player, 1);
        player.setAction(1, PlayerAction.ATTACK);
        player.closeInterface(InterfaceType.PRIMARY_OVERLAY);

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
                        new Option("Lumbridge", () -> p.getMovement().teleport(3224, 3226)),
                        new Option("Seers Village", () -> p.getMovement().teleport(2729, 3492)),
                        new Option("next...", () -> sendTeleportMenu(p, 1))));
                break;

            case 1:
                p.dialogue(new OptionsDialogue("Select a location... [2/2]",
                        new Option("Ardougne", () -> p.getMovement().teleport(2653, 3281)),
                        new Option("Rellekka", () -> p.getMovement().teleport(2658, 3679)),
                        new Option("Edgeville", () -> p.getMovement().teleport(3091, 3494)),
                        new Option(Color.DARK_RED.wrap("Canifis"), () -> p.getMovement().teleport(3511, 3476)),
                        new Option("back...", () -> sendTeleportMenu(p, 0))));
                break;
        }
    }




}