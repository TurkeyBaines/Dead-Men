package io.dm.network.incoming.handlers.commands;

import io.dm.cache.NPCDef;
import io.dm.data.DataFile;
import io.dm.data.impl.npcs.npc_drops;
import io.dm.data.impl.npcs.npc_spawns;
import io.dm.model.World;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.Player;

import java.util.Arrays;

public class NpcCommands {

    public void process(Player player, String... args) {

        switch (args[0]) {

            case "spawn": case "make": case "add":
                spawn(player, args);
                break;

            case "search": case "find": case "get":
                find(player, args);
                break;

            case "search2": case "find2": case "get2":
                find2(player, args);
                break;

            case "pnpc": case "morph":
                morph(player, args);
                break;

            case "reload":
                reload(player);
                break;

        }

    }


    private void spawn(Player player, String... args) {
        int npcId = Integer.valueOf(args[1]);
        int walkRange = 0;
        if (args.length > 2) {
            walkRange = Integer.parseInt(args[2]);
        }
        NPCDef def = NPCDef.get(npcId);
        if(def == null) {
            player.sendMessage("Invalid npc id: " + npcId);
            return;
        }
        new NPC(npcId).spawn(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), walkRange).getCombat().setAllowRespawn(false);
    }

    private void find(Player player, String... args) {
        String search = args[1].replace("_", " ");
        int combat = -1;
        for(NPCDef def : NPCDef.cached.values()) {
            if(def != null && def.name.toLowerCase().contains(search.toLowerCase()) && (combat == -1 || def.combatLevel == combat))
                player.sendMessage(def.id + " (" + def.name + "): combat=" + def.combatLevel + " options=" + Arrays.toString(def.options) +" size=" + def.size);
        }
    }

    private void find2(Player player, String... args) {
        String search = args[1].replace("_", " ");

        for (NPCDef def : NPCDef.cached.values()) {
            if (def == null || def.name == null)
                continue;

            if (def.name.toLowerCase().contains(search)) {
                player.sendMessage("    " + def.id + ": " + def.name);
            }

        }
    }

    private void morph(Player player, String... args) {
        Player p2;
        boolean other = args.length == 3;
        if (other) {
            p2 = World.getPlayer(args[1].replace("_", " "));
            if (p2 == null) return;

            int npcId = Integer.valueOf(args[2]);
            if(npcId > 0) {
                NPCDef def = NPCDef.get(npcId);
                if(def == null) {
                    player.sendMessage("Invalid npc id: " + npcId);
                    return;
                }
                p2.temp.put("LAST_PNPC", npcId);
                p2.getAppearance().setNpcId(npcId);
                p2.sendMessage(def.name + " " + def.size);
            } else {
                p2.getAppearance().setNpcId(-1);
            }
            p2.getAppearance().update();
            return;
        }

        int npcId = Integer.valueOf(args[0]);
        if(npcId > 0) {
            NPCDef def = NPCDef.get(npcId);
            if(def == null) {
                player.sendMessage("Invalid npc id: " + npcId);
                return;
            }
            player.temp.put("LAST_PNPC", npcId);
            player.getAppearance().setNpcId(npcId);
            player.sendMessage(def.name + " " + def.size);
        } else {
            player.getAppearance().setNpcId(-1);
        }
        player.getAppearance().update();
    }

    public void reload(Player player) {
        World.npcs.forEach(NPC::remove); //todo fix this
        DataFile.reload(player, npc_spawns.class);

        NPCDef.forEach(def -> def.lootTable = null);
        DataFile.reload(player, npc_drops.class);
    }

}
