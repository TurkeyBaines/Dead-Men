package io.dm.deadman.tournament.finals;

import io.dm.cache.Color;
import io.dm.deadman.tournament.FinalEvent;
import io.dm.model.World;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.shared.LockType;
import io.dm.model.entity.shared.listeners.DeathListener;
import io.dm.model.map.Bounds;
import io.dm.model.map.Direction;
import io.dm.model.map.dynamic.DynamicMap;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Versus extends FinalEvent {

    Bounds lobby = new Bounds(2535, 4714, 2544, 4718, 0);

    Bracket bracket;
    DynamicMap[] maps;

    @Override
    public void init() {
        bounds = new Bounds(0, 0, 0, 0, 0);

        players.forEach(p -> {
            p.getMovement().teleport(lobby.randomPosition());
            p.sendMessage("Welcome to the Finals! This event is a 1v1.");
            p.sendMessage("Battle your way through the bracket to be the final player standing.");
            p.sendMessage(Color.RED.wrap("You have 2 minutes to gear up before your next fight!"));
            p.sendMessage("Good Luck!");
        });

        World.startEvent(e -> { // 200 ticks = 2 minutes
            e.delay(10); //e.delay(100);
            players.forEach(p -> p.sendMessage(Color.RED.wrap("60 seconds until the next bracket begins!")));
            e.delay(5); //e.delay(50);
            players.forEach(p -> p.sendMessage(Color.RED.wrap("30 seconds until the next bracket begins!")));
            e.delay(5); //e.delay(34);
            players.forEach(p -> p.sendMessage(Color.RED.wrap("10 seconds until the next bracket begins!")));
            e.delay(8); //e.delay(8);
            players.forEach(p -> p.sendMessage(Color.RED.wrap("Prepare to be teleported...")));
            e.delay(8); //e.delay(8);

            bracket = new Bracket().get(players);
            maps = generateMaps();
            movePlayers();
        });
    }

    @Override
    public void update() {
        if (!allMapsDone()) {
            //DO NOTHING AND WAIT!
        } else {
            World.startEvent(e -> { // 200 ticks = 2 minutes
                e.delay(100);
                players.forEach(p -> p.sendMessage(Color.RED.wrap("60 seconds until the next bracket begins!")));
                e.delay(50);
                players.forEach(p -> p.sendMessage(Color.RED.wrap("30 seconds until the next bracket begins!")));
                e.delay(34);
                players.forEach(p -> p.sendMessage(Color.RED.wrap("10 seconds until the next bracket begins!")));
                e.delay(8);
                players.forEach(p -> p.sendMessage(Color.RED.wrap("Prepare to be teleported...")));
                e.delay(8);

                bracket = new Bracket().get(players);
                maps = generateMaps();
                movePlayers();
            });
        }
    }

    private static boolean allowLogout() {
        return false;
    }

    public DynamicMap[] generateMaps() {
        DynamicMap[] maps = new DynamicMap[bracket.length()];

        for (int i = 0; i < bracket.length(); i++) {
            maps[i] = new DynamicMap().build(13133, 1);
        }
        return maps;
    }

    private int[] getSpawnPoints(DynamicMap m) {
        int x = m.convertX(3291);
        int y = m.convertY(4952);
        int x2 = m.convertX(3289);
        int y2 = m.convertY(4963);

        return new int[] {x, y, x2, y2};
    }

    private void movePlayers() {
        for (int i = 0; i < maps.length; i++) {
            DynamicMap m = maps[i];
            Player p1 = bracket.getBracket()[i][0];
            Player p2 = bracket.getBracket()[i][1] != null ? bracket.getBracket()[i][1] : null;


            addDeathListener(m, p1, p2);

            m.assignListener(p1).onEnter(this::onEnter);
            m.assignListener(p2).onEnter(this::onEnter);

            int[] spawns = getSpawnPoints(m);


            p1.getMovement().teleport(spawns[0], spawns[1]);
            p2.getMovement().teleport(spawns[2], spawns[3]);
        }
    }

    private void addDeathListener(DynamicMap m, Player p1, Player p2) {
        m.addEvent(e -> {
            p1.deathEndListener = (DeathListener.SimplePlayer) entity -> {
                players.remove(p1);
                p2.startEvent(e2 -> {
                    p2.setHp(p2.getMaxHp());
                    p2.sendMessage("Congratulations on your win!");
                    p2.sendMessage("You will be teleported back to the lobby in 60 seconds.");
                    p2.sendMessage("Any Items left on the floor will be deleted, please loot before the time runs out!");
                    e2.delay(100);
                    p2.getMovement().teleport(lobby.randomPosition());
                });
            };

            p2.deathEndListener = (DeathListener.SimplePlayer) entity -> {
                players.remove(p2);
                p1.startEvent(e2 -> {
                    p1.setHp(p2.getMaxHp());
                    p1.sendMessage("Congratulations on your win!");
                    p1.sendMessage("You will be teleported back to the lobby in 60 seconds.");
                    p1.sendMessage("Any Items left on the floor will be deleted, please loot before the time runs out!");
                    e2.delay(100);
                    p1.getMovement().teleport(lobby.randomPosition());
                });
            };
        });
    }

    private void onEnter(Player p) {
        p.startEvent(e -> {
            p.lock(LockType.FULL_CANT_ATTACK);
            e.delay(5);
            p.forceText("3...");
            e.delay(2);
            p.forceText("2...");
            e.delay(2);
            p.forceText("1...");
            e.delay(2);
            p.forceText("FIGHT!!!");
            p.unlock();
        });
    }

    private boolean allMapsDone() {
        boolean complete = true;
        if (maps == null)
            return false;
        for (DynamicMap m : maps) {
            if (m != null) complete = false;
        }
        return complete;
    }

    private static class Bracket {

        @Getter private Player[][] bracket;

        public Bracket get(List<Player> playerList) {
            playerList.sort(Comparator.comparing(p -> p.getCombat().getLevel()));

            for (int i = 0; i < playerList.size()/2; i++) {
                bracket[i] = new Player[]{
                        playerList.get(0),
                        playerList.get(1) == null ? playerList.get(1) : null
                };
                playerList.remove(0);
                playerList.remove(1);
            }

            return this;
        }

        public int length() {
            return bracket.length;
        }

        public String getOpponent(Player player) {
            for (Player[] p1 : bracket) {
                if (p1[0] == player) {
                    if (p1[1] == null) return "By Round";
                    return p1[1].getName();
                }
                if (p1[1] == null) {
                    continue;
                }
                if (p1[1] == player) {
                    return p1[0].getName();
                }
            }
            return "By Round";
        }

    }
}
