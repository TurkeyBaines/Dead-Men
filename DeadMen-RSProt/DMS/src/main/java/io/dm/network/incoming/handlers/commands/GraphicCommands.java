package io.dm.network.incoming.handlers.commands;

import io.dm.model.entity.player.Player;

public class GraphicCommands {
    public void process(Player player, String... args) {
        switch (args[0]) {
            case "clear":
                clear(player);
                break;

            case "play":
            case "set":
                play(player, args);
                break;
        }
    }


    public void clear(Player p) {
        p.graphics(-1);
    }

    public void play(Player p, String... args) {
        int length = 1;
        if (args.length == 3)
            length = Integer.parseInt(args[2]);

        int FinalLength = length;
        p.startEvent(e -> {

            for (int i = 0; i < FinalLength; i++) {
                p.graphics(Integer.parseInt(args[1]) + i);
                p.animate(10655);
                e.delay(5);
            }
        });
    }

}
