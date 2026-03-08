package io.dm.network.incoming.handlers.commands;

import io.dm.Server;
import io.dm.deadman.Deadman;
import io.dm.deadman.areas.SafeZone;
import io.dm.deadman.gas.GasArea;
import io.dm.deadman.mutators.impl.StaticGasMutator;
import io.dm.model.entity.player.Player;
import io.dm.model.map.Bounds;
import io.dm.model.map.Position;

public class TestCommands {

    private long serverticks = 0;
    private GasArea gasArea = null;
    private StaticGasMutator gasMutator = null;

    public void process(Player p, String... args) {
        switch (args[0]) {
            case "print":
                print(p, args);
                break;

            case "gas":
                gas(p, args);
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

    private void gas(Player p, String... args) {
        switch (args[1]) {
            case "new":
                gasArea = new GasArea(new Bounds(
                        p.getPosition().getX()-2,
                        p.getPosition().getY()-2,
                        p.getPosition().getX()+2,
                        p.getPosition().getY()+2,
                        0
                ));
                break;

            case "shrink":
                if (gasArea == null) {
                    p.sendMessage("No gasArea variable initialized. Call 'new' first");
                    break;
                }
                gasArea.shrink();
                break;

            case "expand":
                if (gasArea == null) {
                    p.sendMessage("No gasArea variable initialized. Call 'new' first");
                    break;
                }
                gasArea.expand();
                break;

            case "remove":
                if (gasArea == null) {
                    p.sendMessage("No gasArea variable initialized. Call 'new' first");
                    break;
                }
                gasArea.stop = true;
                gasArea.remove();
                break;

            case "region":
                if (args.length == 3 && args[2].equalsIgnoreCase("remove")) {
                    if (gasMutator == null) {
                        p.sendMessage("No Region Gas detected, variable is null.");
                        return;
                    }
                    gasMutator.clear();
                    gasMutator = null;

                } else {
                    if (gasMutator != null) {
                        p.sendMessage("Existing Region Gas detected, remove existing to continue.");
                        return;
                    }
                    gasMutator = new StaticGasMutator();
                    gasMutator.action();
                }
        }

    }

}
