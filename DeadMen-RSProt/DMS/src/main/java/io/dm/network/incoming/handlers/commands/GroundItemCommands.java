package io.dm.network.incoming.handlers.commands;

import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.map.Position;
import io.dm.model.map.ground.GroundItem;

public class GroundItemCommands {

    public void process(Player player, String... args) {
        switch (args[0]) {

            case "pickup": case "get": case "spawn": case "add":
                pickup(player, args);
                break;

        }
    }

    private void pickup(Player player, String[] args) {
        int id = Integer.parseInt(args[1]);
        int quantity = 1;
        if (args.length == 3)
            quantity = Integer.parseInt(args[2]);

        Position pos = player.getPosition();

        if (new Item(id).getDef().stackable) {
            new GroundItem(id, quantity).position(pos.getX(), pos.getY(), pos.getZ()).spawn();
        } else {
            for (int i = 0; i < quantity; i++) {
                new GroundItem(id, 1).position(pos.getX(), pos.getY(), pos.getZ()).spawn();
            }
        }
    }

}
