package io.dm.deadman.tournament.gas;

import io.dm.cache.ObjectDef;
import io.dm.cache.ObjectID;
import io.dm.model.World;
import io.dm.model.combat.Hit;
import io.dm.model.entity.player.Player;
import io.dm.model.map.Bounds;
import io.dm.model.map.MapListener;
import io.dm.model.map.object.GameObject;

import java.util.ArrayList;
import java.util.List;

public class GasArea {

    private static List<Player> players;
    private static Bounds bounds;
    public boolean stop = false;
    private static List<GameObject> fires;
    private static MapListener listener;

    public GasArea(Bounds bounds) {
        players = new ArrayList<>();
        this.bounds = bounds;
        create();
        World.startEvent(e -> {
            while (!stop) {
                if (!players.isEmpty())
                    players.forEach(p -> {
                        damage(p);
                    });
                e.delay(1);
            }
        });
    }

    public void create() {
        ObjectDef.get(ObjectID.BLUE_FIRE).clipType = 0;
        fires = new ArrayList<>();

        listener = MapListener.registerBounds(bounds).onEnter(GasArea::entered).onExit(GasArea::exited);
        bounds.forEachEdgePos(pos -> {
            GameObject fire = new GameObject(ObjectID.BLUE_FIRE, pos.getX(), pos.getY(), bounds.z, 10, 0);
            fire.spawn();
            fires.add(fire);
        });
    }

    public void remove() {
        for (GameObject o : fires)
            o.remove();
        fires = null;
        bounds = null;
        MapListener.remove(listener);
    }

    private static void damage(Player p) {
        p.hit(new Hit().fixedDamage(5));
    }

    public void shrink() {
        int fX = bounds.swX + 1;
        int fY = bounds.swY + 1;
        int sX = (bounds.neX - bounds.swX) - 2;
        int sY = (bounds.neY - bounds.swY) - 2;

        remove();
        ArrayList<Player> p2 = new ArrayList<>();
        bounds = new Bounds(fX, fY, fX+sX, fY+sY, 0);
        players.forEach(p -> {
            if (p != null)
                if (bounds.inBounds(p.getPosition()))
                    p2.add(p);
        });
        players.removeAll(players);
        players.addAll(p2);
        create();
    }

    public void expand() {
        int fX = bounds.swX - 1;
        int fY = bounds.swY - 1;
        int sX = (bounds.neX - bounds.swX) + 2;
        int sY = (bounds.neY - bounds.swY) + 2;

        remove();
        bounds = new Bounds(fX, fY, fX+sX, fY+sY, 0);
        create();
    }

    private static void entered(Player p) {
        System.out.println("Entered!");
        players.add(p);
    }

    private static void exited(Player p, boolean logout) {
        System.out.println("Exited!");
        players.remove(p);
        if (logout) return;
    }

    public static boolean contains(Player player) {
        return bounds.inBounds(player.getPosition());
    }

}
