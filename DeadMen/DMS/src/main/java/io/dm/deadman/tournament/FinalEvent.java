package io.dm.deadman.tournament;

import io.dm.cache.Color;
import io.dm.deadman.Deadman;
import io.dm.deadman.tournament.finals.Versus;
import io.dm.model.World;
import io.dm.model.combat.Hit;
import io.dm.model.combat.HitType;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.shared.LockType;
import io.dm.model.entity.shared.listeners.DeathListener;
import io.dm.model.map.Bounds;

import java.util.ArrayList;
import java.util.List;

public abstract class FinalEvent {

    public List<Player> players;

    public Bounds bounds;

    public boolean spawnedEnding = false;
    public boolean finalFinished = false;

    public Player[] leaderboard = new Player[2];

    public FinalEvent() {
        players = new ArrayList<>();
        for (Player p : World.players) {
            if (p == null) continue;
            if (!Deadman.getOverworld().contains(p)) {
                players.add(p);
                p.sendMessage("You have been added to the Finals! Please wait to be teleported...");

                p.deathStartListener = (DeathListener.SimplePlayer) entity -> {
                    if (players.size() == 3)
                        leaderboard[0] = entity;
                    if (players.size() == 2)
                        leaderboard[1] = entity;
                    players.remove(entity);
                };
            } else {
                p.sendMessage("The Finals have started without you, as you were in the Overworld when it began.");
            }
        }
        System.out.println("Added " + players.size() + " players into the final event.");

    }

    public void end() {
        Player winner = players.get(0);
        NPC reaper = new NPC(5567);

        reaper.spawn(winner.getPosition().getX(), winner.getPosition().getY()+1, winner.getPosition().getZ());
        Hit hit = new Hit(HitType.DAMAGE).fixedDamage(255);

        winner.setHp(winner.getMaxHp());
        winner.startEvent(e -> {
            winner.lock(LockType.FULL);
            winner.animate(862);
            winner.face(reaper);
        });

        reaper.startEvent(e -> {
            reaper.face(winner);
            reaper.forceText("Congratulations on your win, " + winner.getName() + "!");
            e.delay(3);
            reaper.forceText("but now your time has come...");
            e.delay(3);
            reaper.forceText("you will be rewarded substantially in the Overworld!");
            e.delay(2);
            reaper.animate(406);
            e.delay(1);
            winner.hit(hit);
            reaper.animate(2573);
            reaper.forceText("Goodbye, " + winner.getName() + ", until we meet again!");
            e.delay(2);
            winner.unlock();
            reaper.remove();
            e.delay(5);
        });

        World.startEvent(e -> {
            e.delay(15);
            World.players.forEach(p -> p.sendMessage(Color.GOLD.wrap("[Tournament]") + Color.ORANGE_RED.wrap("Tournament Has Finished!")));
            World.players.forEach(p -> p.sendMessage(Color.GOLD.wrap("[Tournament]") + Color.GOLD.wrap("Winner: " + winner.getName())));
            if (leaderboard[1] != null)
                World.players.forEach(p -> p.sendMessage(Color.GOLD.wrap("[Tournament]") + Color.ORANGE.wrap("2nd Place: " + leaderboard[1].getName())));
            if (leaderboard[0] != null)
                World.players.forEach(p -> p.sendMessage(Color.GOLD.wrap("[Tournament]") + Color.RED.wrap("3rd Place: " + leaderboard[0].getName())));
            players.remove(winner);
            finalFinished = true;
        });
    }

    public abstract void init();
    public abstract void update();

    public static FinalEvent getRandom() {
        return new Versus();
    }
}
