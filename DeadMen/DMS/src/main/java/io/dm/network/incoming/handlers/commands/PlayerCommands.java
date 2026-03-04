package io.dm.network.incoming.handlers.commands;

import io.dm.model.World;
import io.dm.model.entity.player.Player;

public class PlayerCommands {

    public void process(Player player, String... args) {
        switch (args[0]) {

            case "god": case "godmode":
                god(player);
                break;

            case "anim":
                anim(player, false, args);
                break;

            case "animall":
                animAll(player, args);
                break;

            case "animx":
                animx(player, args);
                break;

            case "gfx":
                gfx(player, false, args);
                break;

            case "sgfx": case "servergfx": case "servergraphic":
            case "sgraphic":
                gfx(player, true, args);
                break;

            case "debug": case "db":
                debug(player);
                break;

            default:
                Player p2 = World.getPlayer(args[0].replace("_", " "));
                if (p2 == null) {
                    player.sendMessage("This player is offline or you entered the command incorrectly.");
                    return;
                }
                switch (args[1]) {
                    case "god": case "godmode":
                        god(p2);
                        break;

                    case "anim":
                        anim(p2, true, args);
                        break;

                    case "debug": case "db":
                        debug(p2);
                        break;
                }
                break;

        }
    }

    private void god(Player player) {
        if (!player.isInvincible()) {
            player.setInvincible(true);
            player.sendMessage("You have enabled God Mode");
        } else {
            player.setInvincible(false);
            player.sendMessage("You have disabled God Mode");
        }
    }

    private void anim(Player player, boolean other, String... args) {
        if (other) {
            player.animate(Integer.parseInt(args[2]));
        } else {
            player.animate(Integer.parseInt(args[1]));
        }
    }

    private void animx(Player player, String... args) {
        int start = Integer.parseInt(args[1]);
        int iterations = Integer.parseInt(args[2]);
        player.startEvent(e -> {
            for (int i = 0; i < iterations; i++) {
                player.forceText("Anim ID: " + (start+i));
                player.animate(start+i);
                long timeout = 3;
                long curr = 0;
                while (player.isAnimating() && (curr < timeout)) {
                    curr++;
                    e.delay(1);
                }

                player.forceText("! END !");
                e.delay(2);
            }

        });
    }

    private void animAll(Player player, String... args) {
        World.players.forEach(p2 -> { if (p2 != player) p2.animate(Integer.parseInt(args[2])); });
    }

    private void gfx(Player player, boolean server, String... args) {
        int id = Integer.valueOf(args[1]);

        int height = 0;
        if(args.length > 3)
            height = Integer.valueOf(args[1]);

        int delay = 0;
        if(args.length > 1)
            delay = Integer.valueOf(args[2]);

        if (server)
            World.sendGraphics(id, height, delay, player.getPosition());
        else
            player.graphics(id, height, delay);
    }

    private void debug(Player player) {
        player.debug = !player.debug;
        player.sendMessage("Player > Debug: " + player.debug);
    }

}
