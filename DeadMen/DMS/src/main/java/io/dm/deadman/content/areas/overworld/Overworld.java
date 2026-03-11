package io.dm.deadman.content.areas.overworld;

import io.dm.model.entity.player.Player;
import io.dm.model.entity.player.PlayerAction;
import io.dm.model.inter.utils.Config;
import io.dm.model.map.*;
import io.dm.model.map.object.GameObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Overworld {

    private static List<Player> players;
    private HashMap<Integer, Integer> resources;

    private static Bounds bounds = new Bounds(2481,3812,2630,3927, 0);

    public Overworld() {
        players = new ArrayList<>();

        OverworldObjects.register();
        OverworldTools.register();
        OverworldNPCs.register();

        /*
        * Clipping between the Henge's at RC area
        *  - Allows players to stand (NOT WALK!! '_' ) between the Henge!
        * */
        Tile.get(new Position(2524, 3882,0)).clipping = 0;
        Tile.get(new Position(2523, 3880,0)).clipping = 0;

        MapListener.registerBounds(bounds)
                .onEnter(Overworld::entered)
                .onExit(Overworld::exited);
    }

    private static void entered(Player player) {
        players.add(player);
        player.setAction(1, null);
        player.attackPlayerListener = Overworld::allowAttack;
        Config.IN_PVP_AREA.set(player, 0);

    }

    private static void exited(Player player, boolean logout) {
        players.remove(player);
        if (logout) return;
        player.setAction(1, PlayerAction.ATTACK);
        Config.IN_PVP_AREA.set(player, 1);
    }



    private static boolean allowAttack(Player player, Player pTarget, boolean message) {
        return false;
    }

    public boolean contains(Player player) {
        return bounds.inBounds(player.getPosition()) || Region.get(11165).players.contains(player);
    }

    private void resetChest(GameObject object) {
        if (object == null) return;
        Position pos = object.getPosition();
        int dir = object.direction;
        object.remove();
        object = new GameObject(13291, pos.getX(), pos.getY(), pos.getZ(), 10, dir);
        object.spawn();
    }

    public int count() {
        return players.size();
    }
}
