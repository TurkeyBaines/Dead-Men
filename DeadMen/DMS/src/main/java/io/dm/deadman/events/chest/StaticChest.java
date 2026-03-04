package io.dm.deadman.events.chest;

import io.dm.Server;
import io.dm.api.utils.Random;
import io.dm.cache.Color;
import io.dm.cache.Icon;
import io.dm.deadman.events.DMMEvent;
import io.dm.deadman.events.reward.EventRewards;
import io.dm.deadman.events.reward.Reward;
import io.dm.deadman.events.reward.RewardTier;
import io.dm.model.World;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.map.Bounds;
import io.dm.model.map.Position;
import io.dm.model.map.object.GameObject;
import io.dm.model.map.object.actions.ObjectAction;
import io.dm.utility.Broadcast;

public class StaticChest extends DMMEvent {

    private GameObject chest;
    private int maxItemCount;
    private int currentItemsCount;

    private final int[] stage = {
            33120, //SPAWN
            33121, //WAIT FOR TIMER
            33123, //TIMER ELAPSED
            33125  //EVENT FINISHED
    };

    private final long timeout = 1800000;

    private long timerServerTick = 0;

    @Override
    public void spawn() {

        System.out.println("StaticChest:spawn() > running!");

        startTime = System.currentTimeMillis();
        lastUpdate = startTime;

        maxItemCount = Random.get(4)+1;
        currentItemsCount = 0;

        int selection = Random.get(Spawns.values().length-1);
        Spawns spawn = Spawns.values()[selection];
        Position pos = spawn.pos;
        int ori = spawn.orientation;
        String text = spawn.text;

        chest = new GameObject(stage[0], pos, 10, ori);
        World.players.forEach(p -> p.sendMessage(Color.GOLD.wrap("[Tournament] ") + "A " + Color.RED.wrap("Supply Chest") + " has been spotted near " + Color.BLUE.wrap(text) + "."));

        chest.spawn();
        World.startEvent(e -> {

            //Spawn 'CHECK TIMER' chest!
            e.delay(20);
            chest.remove();
            chest = new GameObject(stage[1], pos, 10, ori);
            chest.spawn();

            //5-minute timer before spawning 'UNLOCK' chest!
            timerServerTick = Server.currentTick();
            e.delay(500);
            chest.remove();
            chest = new GameObject(stage[2], pos, 10, ori);
            chest.spawn();
            World.players.forEach(p -> p.sendMessage(Color.GOLD.wrap("[Tournament] ") + "The " + Color.RED.wrap("Supply Chest") + " has unlocked " + Color.BLUE.wrap(text) + "."));
        });

        ObjectAction.register(stage[2], "Loot", ((p, obj) -> lootChest(p)));
        ObjectAction.register(stage[1], "Check timer", ((p, obj) -> timerCheck(p)));
    }

    @Override
    public void update() {
        if (System.currentTimeMillis() > (startTime + timeout)) {
            despawn(); // despawn because event timeout has been achieved
        }

        if (searched() && System.currentTimeMillis() > (lastUpdate + (timeout/3))) {
            despawn(); // despawn because 1/3 of the event timeout has occured since last event update
                        // and the chest has been searched
        }
    }

    @Override
    public void despawn() {

        if (chest != null) {
            chest.remove();
            chest = null;
        }
        eventFinished = true;
    }

    @Override
    public Bounds bounds() {
        return new Bounds(2967, 3341, 2976, 3345, 0);
    }

    private void lootChest(Player p) {
        p.startEvent(e -> {
            int curr = 0;
            int max = 50;
            while (curr < max && chest != null && chest.id == stage[2]) {
                curr++;
                p.face(chest);
                p.animate(535);
                e.delay(2);
            }

            EventRewards.giveReward(p, RewardTier.GOD);
            currentItemsCount++;
            if (currentItemsCount < maxItemCount) {
                return;
            }

            removeChest();
            lastUpdate = System.currentTimeMillis();

        });
    }

    private void removeChest() {
        if (chest == null) return;
        Position pos = chest.getPosition();
        int ori = chest.direction;
        World.startEvent(e -> {
            chest.remove();
            chest = new GameObject(stage[3], pos, 10, ori);
            chest.spawn();
            ObjectAction.register(stage[3], "Search", ((p, obj) -> {
                World.startEvent(event -> {
                    p.lock();
                    p.face(chest);
                    p.sendMessage("You search inside the chest...");
                    e.delay(3);
                    p.sendMessage("You find nothing of value.");
                });
            }));
        });
    }

    private boolean searched() {
        return chest != null && chest.id == stage[3];
    }

    private void timerCheck(Player p) {
        long timerCountdownToTick = timerServerTick + 500;
        double timeAsDbl = (timerCountdownToTick - Server.currentTick()) * 0.6;
        int timeSeconds = (int) timeAsDbl;
        int minutes = timeSeconds / 60;
        int seconds = timeSeconds % 60;

        p.sendMessage("The chest will unlock in " + (minutes > 0 ? (minutes + "m ") : "") + seconds + "s.");
    }

    public enum Spawns {
        AL_PALACE(new Position(3289, 3184, 0), "outside Al-Kharid Palace"),
        AL_MINE(new Position(3295, 3287, 0), "in Al-Kharid Mine"),
        VAR_WESTMINE(new Position(3188, 3412, 0), "next to Varrock West Mine"),
        VAR_EASTMINE(new Position(3276, 3363, 0), "next to Varrock East Mine", 2),
        CATHERBY(new Position(3276, 3363, 0), "near Catherby", 1),
        PORT_KHAZARD(new Position(2621, 3162, 0), "outside Port Khazard", 3),
        CASTLE_WARS(new Position(2443, 3066, 0), "near Castle Wars"),
        RIMMINGTON(new Position(2957, 3220, 0), "in Rimmington", 2),
        DRAYNOR(new Position(3085, 3276, 0), "outside Draynor Village", 2),
        FISHING_GUILD(new Position(2597, 3380, 0), "near the Fishing Guild", 1),
        BARB_VILLAGE(new Position(3080, 3412, 0), "in Barbarian Village", 3);


        Position pos;
        String text;
        int orientation = 0;
        Spawns(Position pos, String text) {
            this.pos = pos;
            this.text = text;
        }
        Spawns(Position pos, String text, int orientation) {
            this.pos = pos;
            this.text = text;
            this.orientation = orientation;
        }
    }
}