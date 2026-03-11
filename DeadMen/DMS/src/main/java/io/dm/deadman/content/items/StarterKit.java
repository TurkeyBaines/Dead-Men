package io.dm.deadman.content.items;

import io.dm.cache.ItemDef;
import io.dm.cache.ItemID;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.item.actions.ItemAction;
import io.dm.utility.TickDelay;

import java.util.ArrayList;
import java.util.HashMap;

public class StarterKit {

    private static HashMap<Player, StarterKit> starterKits;
    private static TickDelay cooldown;
    private static ArrayList<Item> box;

    StarterKit() {
        cooldown = new TickDelay();
        box = new ArrayList<>();
        refresh();
    }

    public void refresh() {
        if (!box.isEmpty())
            box.clear();

        box.add(new Item(ItemID.STARTER_SWORD, 1));
        box.add(new Item(ItemID.STARTER_BOW, 1));
        box.add(new Item(ItemID.STARTER_STAFF, 1));
        box.add(new Item(ItemDef.get(ItemID.TUNA).notedId, 12));
        box.add(new Item(ItemID.DEADMANS_CAPE, 1));
        box.add(new Item(ItemID.ODDSKULL, 1));

        cooldown.delaySeconds(300);
    }

    public void withdraw(Player p) {
        if (cooldown == null || cooldown.finished()) {
            refresh();
        } else if (!cooldown.finished()) {
            p.sendMessage("You must wait a short while before taking more items out of the box.");
            return;
        }

        for (Item i : box) {
            if (p.getInventory().hasFreeSlots(1)) {
                p.getInventory().add(i);
                box.remove(i);
            }
        }
    }

    public void peek(Player p) {
        StringBuilder sb = new StringBuilder();
        sb.append("The box contains: ");
        for (Item i : box) {
            sb.append((i.getAmount() > 1 ? i.getAmount() + " x " : "") + i.getDef().name + ", ");
        }
    }

    static {
        starterKits = new HashMap<>();

        ItemAction.registerInventory(ItemID.STARTER_KIT, "Take-All", (p, i) -> {
            if (!starterKits.containsKey(p)) starterKits.put(p, new StarterKit());
            starterKits.get(p).withdraw(p);
        });

        ItemAction.registerInventory(ItemID.STARTER_KIT, "Peek", (p, i) -> {
            if (!starterKits.containsKey(p)) starterKits.put(p, new StarterKit());
            starterKits.get(p).peek(p);
        });
    }
}