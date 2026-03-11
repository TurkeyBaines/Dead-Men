package io.dm.network.incoming.handlers.commands;

import io.dm.deadman.Deadman;
import io.dm.deadman.tournament.events.DMMEvent;
import io.dm.deadman.tournament.events.breach.Breach;
import io.dm.deadman.tournament.events.chest.StaticChest;
import io.dm.deadman.tournament.events.gold_cart.GoldCart;
import io.dm.deadman.tournament.Tournament;
import io.dm.deadman.tournament.stages.Final;
import io.dm.deadman.tournament.stages.Lobby;
import io.dm.deadman.tournament.stages.Main;
import io.dm.model.World;
import io.dm.model.entity.player.Player;

public class TournamentCommands {

    public void process(Player player, String... args) {
        switch (args[0]) {

            case "print":
                print(player, args);
                break;

            case "setstate": case "state":
                setState(player, args);
                break;

            case "event":
                event(player, args);
                break;

        }
    }

    private void print(Player p, String... args) {
        switch (args[1]) {

            case "state":
                p.sendMessage("Current Stage: " + Deadman.getStage().stageName().name());
                break;

            case "overworld": case "over": case "ow":
                p.sendMessage("Players in Overworld: " + Deadman.getOverworld().count());
                break;

            case "citadel": case "cit":
                p.sendMessage("Players in Citadel: " + Deadman.getCitadel().count());
                break;

            case "remaining":
                if (Deadman.getStage().stageName() == Tournament.StageName.FINAL) {
                    Final f = (Final) Deadman.getStage();
                    p.sendMessage("Players Alive in Final: " + f.getEvent().players.size());
                }
                break;

            case "final":
                if (Deadman.getStage().stageName() == Tournament.StageName.FINAL) {
                    Final f = (Final) Deadman.getStage();

                    p.sendMessage("Running: " + ((System.currentTimeMillis() - f.startTime) / 1000) + " seconds");
                    p.sendMessage("Remaining: " + f.getEvent().players.size());
                    p.sendMessage("Reaper Spawned: " + f.getEvent().spawnedEnding);
                    p.sendMessage("Finished: " + f.getEvent().finalFinished);
                }
                break;

            case "time":
                p.sendMessage("Start Time: " + ((System.currentTimeMillis() - Deadman.getStage().startTime) / 1000) + " seconds ago");
                p.sendMessage("Next Time: " + (((Deadman.getStage().startTime + Deadman.getStage().duration) - System.currentTimeMillis()) / 1000) + " seconds");
                if (Deadman.getStage().stageName() == Tournament.StageName.MAIN) {
                    Main m = (Main) Deadman.getStage();
                    if (m.currentEvent == null) {
                        p.sendMessage("Next Event: " + ((m.nextEvent - System.currentTimeMillis()) / 1000) + " seconds");
                    } else {
                        p.sendMessage("Next Event: Event Already Running!");
                    }
                }
                break;
        }
    }

    private void setState(Player p, String... args) {
        switch (args[1].toLowerCase()) {
            case "lobby":
                Deadman.setStage(new Lobby());
                p.sendMessage("Set State to LOBBY");
                break;

            case "main":
                Deadman.setStage(new Main());
                p.sendMessage("Set State to MAIN");
                break;

            case "final":
                Deadman.setStage(new Final());
                p.sendMessage("Set State to FINAL");
                break;

            default:
                p.sendMessage("Invalid Tournament State! [ LOBBY / MAIN / FINAL ]");
                break;
        }
        Deadman.getStage().onLoad();
    }

    private void event(Player player, String... args) {
        switch (args[1]) {

            case "kill": case "end":
                if (Deadman.getStage().stageName() != Tournament.StageName.MAIN) {
                    player.sendMessage("Not in Main Stage!");
                    return;
                }

                Main m = (Main) Deadman.getStage();
                if (m == null) return;
                if (m.currentEvent == null) {
                    player.sendMessage("No event to kill");
                    return;
                }

                m.currentEvent.despawn();
                m.currentEvent = null;
                m.nextEvent = System.currentTimeMillis() + 900000;
                break;

            case "cart":
                if (args[2].equalsIgnoreCase("test"))
                    spawnTestEvent(new GoldCart());
                else if (args[2].equalsIgnoreCase("live"))
                    spawnLiveEvent(player, new GoldCart());
                break;

            case "chest":
                if (args[2].equalsIgnoreCase("test"))
                    spawnTestEvent(new StaticChest());
                else if (args[2].equalsIgnoreCase("live"))
                    spawnLiveEvent(player, new StaticChest());
                break;

            case "breach":
                if (args[2].equalsIgnoreCase("test"))
                    spawnTestEvent(new Breach());
                else if (args[2].equalsIgnoreCase("live"))
                    spawnLiveEvent(player, new Breach());
                break;

        }
    }

    private void spawnTestEvent(DMMEvent event) {
        event.spawn();
        World.players.forEach(p -> p.getMovement().teleport(event.bounds().neX, event.bounds().neY, event.bounds().z));
        System.out.println("A TEST DMM EVENT HAS BEEN SPAWNED, YOU HAVE BEEN TELEPORTED TO IT!");
    }

    private void spawnLiveEvent(Player p, DMMEvent e) {
        if (Deadman.getStage().stageName() == Tournament.StageName.MAIN) {

            Main m = (Main) Deadman.getStage();

            if (m.currentEvent != null) {
                p.sendMessage("There is already an active event ongoing!");
                return;
            }

            m.currentEvent = e;
            m.nextEvent += 120000;
            e.spawn();

        }
    }
}

