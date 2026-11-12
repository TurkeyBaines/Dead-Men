package io.dm.data.impl;

import com.google.gson.annotations.Expose;
import io.dm.api.utils.JsonUtils;
import io.dm.data.DataFile;
import io.dm.model.World;
import io.dm.model.entity.npc.actions.edgeville.Nurse;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.player.PlayerCounter;
import io.dm.model.entity.shared.LockType;
import io.dm.model.inter.dialogue.MessageDialogue;
import io.dm.model.inter.dialogue.YesNoDialogue;
import io.dm.model.item.Item;
import io.dm.model.map.Position;

import java.util.Map;

import static io.dm.cache.ItemID.BLOOD_MONEY;

public class teleports extends DataFile {

    public static Category[] CATEGORIES;

    @Override
    public String path() {
        return "teleports_" + World.type.name().toLowerCase() + ".json";
    }

    @Override
    public int priority() {
        return 1;
    }

    @Override
    public Object fromJson(String fileName, String json) {
        Map<String, Subcategory[]> categories = JsonUtils.fromJson(json, Map.class, String.class, Subcategory[].class);
        CATEGORIES = new Category[categories.size()];
        int i = 0;
        for(Map.Entry<String, Subcategory[]> entry : categories.entrySet()) {
            Category category = new Category();
            category.name = entry.getKey();
            category.subcategories = entry.getValue();
            CATEGORIES[i++] = category;
        }
        return categories;
    }

    public static final class Category {
        public String name;
        public Subcategory[] subcategories;
    }

    public static final class Subcategory {
        @Expose public String name;
        @Expose public Teleport[] teleports;
    }

    public static final class Teleport {
        @Expose public String name;
        @Expose public int x, y, z;
        @Expose public int price;
    }

    /**
     * Teleporting
     */

    public static void previous(Player player) {
        if(player.previousTeleportX == -1)
            player.dialogue(new MessageDialogue("You haven't teleported anywhere yet."));
        else
            teleportStart(player, player.previousTeleportX, player.previousTeleportY, player.previousTeleportZ, player.previousTeleportPrice);
    }

    private static void teleportStart(Player player, int x, int y, int z, int price) {
        if(price == 0) {
            teleportFinish(player, x, y, z, price);
            return;
        }
        player.unsafeDialogue(new YesNoDialogue("Are you sure you want to use this teleport?", "This teleport will cost " + price + " blood money.", BLOOD_MONEY, price, () -> {
            Item bloodMoneyInventory = player.getInventory().findItem(BLOOD_MONEY);
            if(bloodMoneyInventory != null && bloodMoneyInventory.getAmount() >= price) {
                bloodMoneyInventory.remove(price);
                player.sendFilteredMessage("<col=6f0000>You give the Wizard " + price + " blood money.");
                teleportFinish(player, x, y, z, price);
                return;
            }
            Item bloodMoneyBank = player.getBank().findItem(BLOOD_MONEY);
            if(bloodMoneyBank != null && bloodMoneyBank.getAmount() >= price) {
                bloodMoneyBank.remove(price);
                player.sendFilteredMessage("<col=6f0000>The Wizard collects " + price + " blood money from your bank.");
                teleportFinish(player, x, y, z, price);
                return;
            }
            player.sendFilteredMessage("<col=6f0000>You don't have enough blood money to use this teleport.");
        }));
    }

    private static void teleportFinish(Player player, int x, int y, int z, int price) {
        player.previousTeleportX = x;
        player.previousTeleportY = y;
        player.previousTeleportZ = z;
        player.previousTeleportPrice = price;
        teleport(player, x, y, z);
    }

    public static void teleport(Player player, int x, int y, int z) {
        player.resetActions(true, true, true);
        player.lock(LockType.FULL_NULLIFY_DAMAGE); //keep lock outside of event!
        player.startEvent(e -> {
            player.getPacketSender().fadeOut();
            e.delay(2);
            player.getMovement().teleport(x, y, z);
            player.getPacketSender().clearFade();
            Nurse.heal(player, null);
            PlayerCounter.TELEPORT_PORTAL_USES.increment(player, 1);
            player.getPacketSender().fadeIn();
            player.unlock();
        });
    }

    public static void teleport(Player player, Position position) {
        teleport(player, position.getX(), position.getY(), position.getZ());
    }

}