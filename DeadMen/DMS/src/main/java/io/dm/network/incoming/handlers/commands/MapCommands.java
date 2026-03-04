package io.dm.network.incoming.handlers.commands;

import io.dm.model.World;
import io.dm.model.entity.player.Player;
import io.dm.model.map.Position;
import io.dm.model.map.Region;
import io.dm.model.map.Tile;
import io.dm.model.map.route.RouteFinder;

public class MapCommands {

    public void process(Player player, String... args) {

        switch (args[0]) {

            case "loc": case "pos":
                loc(player);
                break;

            case "chunk":
                chunk(player);
                break;

            case "region":
                region(player, args);
                break;

            case "toregion":
                toRegion(player, args);
                break;

            case "clipping":
                clipping(player);
                break;

            case "tele": case "move": case "moveme":
                move(player, args);
                break;

            case "teleother": case "moveother": case "ptele":
            case "pmove":
                move(player, args);
                break;

            case "xto": case "teleto": case "tpto":
                teleTo(player, args);
                break;

            case "height": case "h":
                height(player, args);
                break;
        }
    }

    private void loc(Player player) {
        player.sendMessage("Abs: " + player.getPosition().getX() + ", " + player.getPosition().getY() + ", " + player.getPosition().getZ());
    }

    private void chunk(Player player) {
        int chunkX = player.getPosition().getChunkX();
        int chunkY = player.getPosition().getChunkY();
        int chunkAbsX = chunkX << 3;
        int chunkAbsY = chunkY << 3;
        int localX = player.getPosition().getX() - chunkAbsX;
        int localY = player.getPosition().getY() - chunkAbsY;
        Region region = Region.get(chunkAbsX, chunkAbsY);
        int pointX = (player.getPosition().getX() - region.baseX) / 8;
        int pointY = (player.getPosition().getY() - region.baseY) / 8;
        player.sendMessage("Chunk: " + chunkX + ", " + chunkY);
        player.sendMessage("    abs = " + chunkAbsX + ", " + chunkAbsY);
        player.sendMessage("    local = " + localX + ", " + localY);
        player.sendMessage("    points =  " + pointX + ", " + pointY);
    }

    private void region(Player player, String... args) {
        Region region;
        if(args == null || args.length == 1)
            region = player.getPosition().getRegion();
        else
            region = Region.get(Integer.valueOf(args[1]));
        player.sendMessage("Region: " + region.id);
        player.sendMessage("    base = " + region.baseX + "," + region.baseY);
        player.sendMessage("    empty = " + region.empty);
    }

    private void toRegion(Player player, String... args) {
        int region = (Integer.parseInt(args[1]));
        int x = ((region << 6) >> 8);
        int y = (region << 6);

        player.getMovement().teleport(x, y, player.getHeight());
    }

    private void clipping(Player player) {
        Tile tile = Tile.get(player.getAbsX(), player.getAbsY(), player.getHeight(), false);
        player.sendMessage("Clipping: " + (tile == null ? -1 : tile.clipping));
        System.out.println(tile.clipping & ~RouteFinder.UNMOVABLE_MASK);
    }

    private void move(Player player, String... args) {
        int x, y, z = player.getHeight();
        x = Integer.parseInt(args[1]);
        y = Integer.parseInt(args[2]);
        if (args.length == 4) z = Integer.parseInt(args[3]);

        player.getMovement().teleport(x, y, z);
    }

    private void moveOther(Player player, String... args) {
        int x, y, z = player.getHeight();
        x = Integer.parseInt(args[2]);
        y = Integer.parseInt(args[3]);
        if (args.length == 5) z = Integer.parseInt(args[4]);
        Player p2 = World.getPlayer(args[1].replace("_", " "));
        if (p2 == null) return;
        p2.getMovement().teleport(x, y, z);
    }

    private void teleTo(Player player, String... args) {
        Player p2 = World.getPlayer(args[1].replace("_", " "));
        if (p2 == null) return;

        player.getMovement().teleport(p2.getAbsX(), p2.getAbsY(), p2.getHeight());
    }

    private void height(Player player, String... args) {
        if (args[1].equalsIgnoreCase("up")) {
            if (player.getHeight() < 3)
                player.getMovement().teleport(player.getAbsX(), player.getAbsY(), player.getHeight()+1);
        } else if (args[1].equalsIgnoreCase("down")) {
            if (player.getHeight() >= 0)
                player.getMovement().teleport(player.getAbsX(), player.getAbsY(), player.getHeight()-1);
        } else {
            int x = Integer.parseInt(args[1]);
            if (x < 0) x = 0;
            if (x > 3) x = 3;
            player.getMovement().teleport(player.getAbsX(), player.getAbsY(), x);
        }
    }



}
