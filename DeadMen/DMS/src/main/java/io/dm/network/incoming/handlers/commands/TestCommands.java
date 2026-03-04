package io.dm.network.incoming.handlers.commands;

import io.dm.Server;
import io.dm.model.entity.player.Player;

public class TestCommands {

    private long serverticks = 0;

    public void process(Player p, String... args) {
        switch (args[0]) {
            case "print":
                print(p, args);
                break;
        }
    }


    private void print(Player p, String... args) {

        switch (args[1]) {

            case "servertick":
                p.sendMessage("Current Tick: " + Server.currentTick());
                p.sendMessage("Time since last check: " + (Server.currentTick() - serverticks));
                serverticks = Server.currentTick();
                break;

        }

    }

}
