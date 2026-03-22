package io.dm.deadman.content.guard;

import io.dm.model.World;
import io.dm.model.entity.player.Player;
import io.dm.utility.TickDelay;

import java.util.ArrayList;
import java.util.HashMap;

public class Skull {

    public static final int SHORT_SKULL = 500;
    public static final int MED_SKULL = 750;
    public static final int LONG_SKULL = 1000;

    private static HashMap<Player, Record> playerList;
    private static ArrayList<Player> remove;

    static {
        playerList = new HashMap<>();
        remove = new ArrayList<>();

        World.startEvent(e -> {
            while (true) {
                e.delay(10);
                if (!playerList.isEmpty())
                    tick();
            }
        });
    }

    public static void tick() {

        for (Record r : playerList.values()) {
            System.out.println("[Skull] " + r.player.getName() + ": " + r.timer.remaining());
            if (r.complete()) {
                System.out.println("[Skull] Removing " + r.player.getName());
                remove.add(r.player);
                r.player.getAppearance().setSkullIcon(-1);
            }
        }

        for (Player p : remove) {
            playerList.remove(p);
        }
        remove.clear();

    }

    public static void onDeath(Player p) {
        if (hasSkull(p)) {
            remove.add(p);
            p.getAppearance().setSkullIcon(-1);
        }
    }

    private static class Record {
        public final Player player;
        public TickDelay timer;

        public Record(Player player, int delay) {
            this.player = player;
            timer = new TickDelay();
            timer.delay(delay);
        }

        public void addDelay(int ticks) {
            timer.addDelay(ticks);
            if (timer.remaining() > 1500) {
                timer.setEnd(1500);
            }
        }

        public boolean complete() {
            return timer.finished();
        }
    }

    public static boolean hasSkull(Player p) {
        return playerList.containsKey(p);
    }

    public static void skull(Player p, int duration) {
        if (!hasSkull(p)) {
            playerList.put(p, new Record(p, duration));
            p.getAppearance().setSkullIcon(0);
            return;
        }

        playerList.get(p).addDelay(duration);
    }

}
