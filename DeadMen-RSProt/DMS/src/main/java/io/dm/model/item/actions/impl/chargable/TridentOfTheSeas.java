package io.dm.model.item.actions.impl.chargable;

import io.dm.api.utils.NumberUtils;
import io.dm.cache.Color;
import io.dm.cache.ItemDef;
import io.dm.model.combat.Hit;
import io.dm.model.entity.Entity;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.ItemDialogue;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.utils.Option;
import io.dm.model.item.Item;
import io.dm.model.item.actions.ItemAction;
import io.dm.model.item.actions.ItemItemAction;
import io.dm.model.item.attributes.AttributeExtensions;
import io.dm.model.item.attributes.AttributeTypes;
import io.dm.model.skills.magic.rune.Rune;

import java.util.Arrays;
import java.util.List;

import static io.dm.cache.ItemID.COINS_995;

public class TridentOfTheSeas {

    public static final int FULLY_CHARGED = 11905;
    public static final int CHARGED = 11907;
    public static final int UNCHARGED = 11908;
    private static List<Item> CHARGE_ITEMS;

    static {
        CHARGE_ITEMS = Arrays.asList(Rune.DEATH.toItem(1), Rune.CHAOS.toItem(1), Rune.FIRE.toItem(5), new Item(COINS_995, 250)); //250gp
        for (Item item : CHARGE_ITEMS) {
            ItemItemAction.register(CHARGED, item.getId(), TridentOfTheSeas::charge);
            ItemItemAction.register(UNCHARGED, item.getId(), TridentOfTheSeas::charge);
        }
        for (int id : Arrays.asList(FULLY_CHARGED, CHARGED)) {
            ItemAction.registerInventory(id, "check", TridentOfTheSeas::check);
            ItemAction.registerEquipment(id, "check", TridentOfTheSeas::check);
            ItemAction.registerInventory(id, "uncharge", TridentOfTheSeas::uncharge);
            ItemDef.get(id).addPreTargetDefendListener(TridentOfTheSeas::consumeCharge);
        }
        ItemAction.registerInventory(CHARGED, "drop", TridentOfTheSeas::drop);
    }

    private static void consumeCharge(Player player, Item item, Hit hit, Entity entity) {
        if (item.getId() == FULLY_CHARGED) {
            item.setId(CHARGED);
            item.putAttribute(AttributeTypes.CHARGES, 2499);
            player.getCombat().updateWeapon(false);
        } else {
            int charges = AttributeExtensions.getCharges(item);
            AttributeExtensions.deincrementCharges(item, 1);
            if (charges <= 0) {
                item.setId(UNCHARGED);
                item.clearAttribute(AttributeTypes.CHARGES);
                player.sendMessage(Color.RED.wrap("Your Trident of the Seas has ran out of charges!"));
                player.getCombat().updateWeapon(false);
            }
        }
    }

    private static void drop(Player player, Item item) {
        player.dialogue(new OptionsDialogue("You cannot drop this item. Uncharge it instead?",
                new Option("Yes, uncharge it.", () -> uncharge(player, item)),
                new Option("No.")));
    }

    public static void check(Player player, Item item) {
        if (item.getId() == FULLY_CHARGED)
            player.sendMessage("Your Trident of the Seas is fully charged.");
        else
            player.sendMessage("Your Trident of the Seas has " + AttributeExtensions.getCharges(item) + " charges remaining.");
    }

    public static void uncharge(Player player, Item item) {
        String lostItem = CHARGE_ITEMS.get(3).getDef().name.toLowerCase();
        player.dialogue(new OptionsDialogue("You will NOT get the " + lostItem + " back.",
                new Option("Okay. uncharge it.", () -> {
                    if (player.getInventory().getFreeSlots() < 3) {
                        player.sendMessage("You'll need at least 3 free inventory spaces to do this.");
                        return;
                    }
                    int charges = AttributeExtensions.getCharges(item);
                    for (int i = 0; i < CHARGE_ITEMS.size() - 1; i++) { // dont refund the last item
                        Item rune = CHARGE_ITEMS.get(i);
                        player.getInventory().add(rune.getId(), rune.getAmount() * charges);
                    }
                    AttributeExtensions.setCharges(item, 0);
                    item.setId(UNCHARGED);
                }),
                new Option("No, don't uncharge it.")
        ));
    }

    public static void charge(Player p, Item trident, Item other) {
        int currentCharges = trident.getId() == 11908 ? 0 : AttributeExtensions.getCharges(trident);
        if (currentCharges >= 2500) {
            p.sendMessage("Your trident can't hold any more charges.");
            return;
        }
        int inventoryCharges = CHARGE_ITEMS.stream().mapToInt(i -> p.getInventory().getAmount(i.getId()) / i.getAmount()).min().getAsInt();
        if (inventoryCharges == 0) {
            p.sendMessage("The Trident of the Seas requires 1 death rune, 1 chaos rune, 5 fire runes and " + NumberUtils.formatNumber(CHARGE_ITEMS.get(3).getAmount()) + " " + CHARGE_ITEMS.get(3).getDef().name.toLowerCase() + " for each charge.");
            return;
        }
        int chargesToAdd = Math.min(inventoryCharges, 2500 - currentCharges);
        CHARGE_ITEMS.forEach(i -> p.getInventory().remove(i.getId(), i.getAmount() * chargesToAdd));
        p.animate(1979);
        p.graphics(1250, 25, 0);
        if (chargesToAdd + currentCharges == 2500) {
            trident.clearAttribute(AttributeTypes.CHARGES);
            trident.setId(FULLY_CHARGED);
            p.dialogue(new ItemDialogue().one(CHARGED, "Your Trident of the Seas is now fully charged."));
        } else {
            trident.setId(CHARGED);
            trident.putAttribute(AttributeTypes.CHARGES, chargesToAdd + currentCharges);
            p.dialogue(new ItemDialogue().one(CHARGED, "You charge your Trident of the Seas. It now has " + NumberUtils.formatNumber(chargesToAdd+currentCharges) + " charges."));
        }
    }

}
