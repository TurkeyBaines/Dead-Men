package io.dm.network.incoming.handlers.commands;

import io.dm.cache.ItemDef;
import io.dm.data.DataFile;
import io.dm.data.impl.items.item_info;
import io.dm.data.impl.items.shield_types;
import io.dm.data.impl.items.weapon_types;
import io.dm.data.yaml.YamlLoader;
import io.dm.data.yaml.impl.ShopLoader;
import io.dm.model.World;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.MessageDialogue;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.utils.Option;
import io.dm.model.item.Item;
import io.dm.model.item.containers.Inventory;

import java.util.Arrays;

public class ItemCommands {

    public void process(Player player, String... args) {
        switch (args[0]) {

            case "pickup": case "get": case "spawn":
                pickup(player, args);
                break;

            case "find": case "search":
                find(player, args);
                break;

            case "clear": case "empty":
                clear(player);
                break;

            case "clearother": case "emptyother":
            case "pclear": case "pempty":
                clear(player, args);
                break;

            case "def": case "iteminfo":
                def(player, args);
                break;

            case "copy":
                copy(player, args);
                break;

            case "reload":
                reload(player);
                break;
        }
    }


    private void pickup(Player player, String[] args) {
        int id = Integer.parseInt(args[1]);
        int quantity = 1;
        if (args.length == 3)
            quantity = Integer.parseInt(args[2]);

        player.getInventory().add(id, quantity);
    }

    private void find(Player player, String[] args) {
        String search = args[1].replace("_", " ").toLowerCase();
        int found = 0;
        ItemDef exactMatch = null;
        for(ItemDef def : ItemDef.cached.values()) {
            if(def == null || def.name == null)
                continue;
            if(def.isNote() || def.isPlaceholder())
                continue;
            String name = def.name.toLowerCase();
            if(name.contains(search)) {
                player.sendFilteredMessage("    " + def.id + ": " + def.name);
            }
            if(name.equals(search)) {
                if(exactMatch == null)
                    exactMatch = def;
            }
        }
        if(exactMatch != null) {
            player.sendFilteredMessage("Most relevant result for '" + search + "':");
            player.sendFilteredMessage("    " + exactMatch.id + ": " + exactMatch.name);
            player.getInventory().add(exactMatch.id, 1);
        }
    }

    private void clear(Player player) {
        player.dialogue(
                new MessageDialogue("Warning! This removes all items from your account"),
                new OptionsDialogue("Are you sure you wish to preform this action?",
                        new Option("Yes", () -> {
                            player.getInventory().clear();
                            player.getEquipment().clear();
                            player.getBank().clear();
                        }),
                        new Option("No", () -> player.sendFilteredMessage("You did not empty your account.")))
        );
    }

    private void clear(Player player, String... args) {
        Player p2 = World.getPlayer(args[1].replace("_", " "));
        if (p2 == null) return;

        player.dialogue(
                new MessageDialogue("Warning! This removes all items from " + p2.getName() + "'s inventory"),
                new OptionsDialogue("Are you sure you wish to preform this action?",
                        new Option("Yes", () -> {
                            p2.getInventory().clear();
                            p2.getEquipment().clear();
                            p2.getBank().clear();
                        }),
                        new Option("No", () -> player.sendFilteredMessage("You did not empty their inventory.")))
        );
    }

    private void def(Player player, String... args) {
        ItemDef def = ItemDef.get(Integer.parseInt(args[1]));
        if (def == null) {
            player.sendMessage("Invalid id!");
            return;
        }
        player.sendMessage("inventory=" +def.inventoryModel);
        player.sendMessage("origcolors=" + Arrays.toString(def.colorFind));
        player.sendMessage("replacecolors=" + Arrays.toString(def.colorReplace));
        player.sendMessage("model=" + def.anInt1504);
    }

    private void copy(Player player, String... args) {
        Player p2 = World.getPlayer(args[1].replace("_", " "));
        if (p2 == null) return;
        Inventory p2Inv = p2.getInventory();
        if (p2Inv == null) return;
        player.getInventory().clear();

        for (Item i : p2Inv.getItems()) {
            player.getInventory().add(i);
        }

        player.sendMessage("Cleared your Inventory & Copied from " + p2.getName());
    }

    private void reload(Player player){
        new Thread(() -> {
            player.sendMessage("Reloading item info...");
            DataFile.reload(player, shield_types.class);
            DataFile.reload(player, weapon_types.class);
            DataFile.reload(player, item_info.class);

            YamlLoader.load(Arrays.asList(new ShopLoader()));
            player.sendMessage("Done!");
        }).start();
    }

}
