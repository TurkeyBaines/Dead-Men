package io.dm.network.incoming.handlers.commands;

import io.dm.cache.ObjectDef;
import io.dm.data.DataFile;
import io.dm.data.impl.objects.object_spawns;
import io.dm.model.entity.player.Player;
import io.dm.model.map.object.GameObject;

import java.util.Arrays;
import java.util.Objects;

public class ObjectCommands {

    public void process(Player player, String... args) {
        switch (args[0]) {

            case "spawn": case "add": case "make":
                spawn(player, false, args);
                break;

            case "sspawn": case "sadd": case "smake":
                spawn(player, true, args);
                break;

            case "def":
                def(player, args);
                break;

            case "find": case "fobj": case "findobj":
            case "search":
                find(player, args);
                break;

            case "reload":
                reload(player);
                break;
        }
    }

    private void spawn(Player player, boolean server, String... args) {
        if (server) {
            int id = Integer.valueOf(args[1]);
            int type = 10;
            if(args.length > 2)
                type = Integer.valueOf(args[2]);
            int direction = 0;
            if(args.length > 3)
                direction = Integer.valueOf(args[3]);
            GameObject.spawn(id, player.getPosition(), type, direction);
        } else {
            int id = Integer.valueOf(args[1]);
            int type = 10;
            if(args.length > 2)
                type = Integer.valueOf(args[2]);
            int direction = 0;
            if(args.length > 3)
                direction = Integer.valueOf(args[3]);
            player.getPacketSender().sendCreateObject(id, player.getAbsX(), player.getAbsY(), player.getHeight(), type, direction);
        }
    }

    private void def(Player player, String... args) {
        ObjectDef def = ObjectDef.get(Integer.valueOf(args[1]));
        if(def == null) return;

        for(int i = 0; i < def.showIds.length; i++) {
            int id = def.showIds[i];
            ObjectDef obj = ObjectDef.get(id);
            if(obj == null)
                continue;
            System.out.println("[" + i + "]: \"" + obj.name + "\" #" + id + "; options=" + Arrays.toString(obj.options));
        }
    }

    private void find(Player player, String... args) {
        ObjectDef.LOADED.values().stream()
                .filter(Objects::nonNull)
                .filter(def -> !def.name.isEmpty())
                .filter(def -> args[1].toLowerCase().contains(def.name.toLowerCase()))
                .forEachOrdered(def -> player.sendMessage(def.id +" - "+ def.name));

    }

    private void reload(Player player) {
        DataFile.reload(player, object_spawns.class);
    }

}
