package io.dm.model.item.actions.impl.chargable;

import io.dm.api.utils.NumberUtils;
import io.dm.cache.ItemDef;
import io.dm.model.combat.RangedData;
import io.dm.model.combat.RangedWeapon;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.ItemDialogue;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.dialogue.YesNoDialogue;
import io.dm.model.inter.utils.Option;
import io.dm.model.item.Item;
import io.dm.model.item.actions.ItemAction;
import io.dm.model.item.actions.ItemItemAction;
import io.dm.model.item.attributes.AttributeExtensions;
import io.dm.model.item.attributes.AttributeTypes;
import io.dm.model.skills.Tool;
import io.dm.model.stat.StatType;

import static io.dm.cache.ItemID.MAGMA_MUTAGEN;
import static io.dm.model.skills.Tool.CHISEL;

public class Blowpipe {

    private static final int SCALES = 12934;
    private static final int UNCHARGED = 12924, CHARGED = 12926;
    private static final int MAX_AMOUNT = 0x3fff;
    private static final int TAZANITE_FANG = 12922;

    public enum Dart {

        NONE(-1, null),
        BRONZE(806, RangedWeapon.BRONZE_DART.data),
        IRON(807, RangedWeapon.IRON_DART.data),
        STEEL(808, RangedWeapon.STEEL_DART.data),
        BLACK(3093, RangedWeapon.BLACK_DART.data),
        MITHRIL(809, RangedWeapon.MITHRIL_DART.data),
        ADAMANT(810, RangedWeapon.ADAMANT_DART.data),
        RUNE(811, RangedWeapon.RUNE_DART.data),
        DRAGON(11230, RangedWeapon.DRAGON_DART.data);

        public final int id;

        public final RangedData rangedData;

        Dart(int id, RangedData rangedData) {
            this.id = id;
            this.rangedData = rangedData;
        }

    }

    /**
     * Dismantle (Uncharged)
     */

    private static void dismantle(Player player, Item blowpipe) {
        player.dialogue(
                new OptionsDialogue(
                        "Dismantle Toxic blowpipe (empty)",
                        new Option("Yes", () -> {
                            if(!player.getInventory().contains(blowpipe))
                                return;
                            blowpipe.remove();
                            player.getInventory().add(SCALES, 20000);
                            player.closeDialogue();
                        }),
                        new Option("No", player::closeDialogue)
                )
        );
    }

    /**
     * Check
     */

    private static void check(Player player, Item blowpipe) {
        String darts;
        Dart dart = getDart(blowpipe);
        if(dart == Dart.NONE)
            darts = "None";
        else
            darts = ItemDef.get(dart.id).name + " x " + getDartAmount(blowpipe);
        String scales;
        int scalesAmount = getScalesAmount(blowpipe);
        System.out.println("Scales: " + scalesAmount); //prints out 16380 here
        if(scalesAmount == 0)
            scales = "0.0%, 0 scales";
        else
            scales = NumberUtils.formatOnePlace(((double) scalesAmount / MAX_AMOUNT) * 100D) + "%, " + scalesAmount + " scales";
        player.sendMessage("Darts: <col=007f00>" + darts + "</col> Scales: <col=007f00>" + scales + "</col>");
    }

    /**
     * Load/Unload
     */

    private static void load(Player player, Item blowpipe, Item dartItem, Dart dart) {
        Dart loadedDart = getDart(blowpipe);
        if(loadedDart != Dart.NONE && loadedDart != dart) {
            player.sendMessage("The blowpipe currently contains a different sort of dart.");
            return;
        }
        int dartAmount = getDartAmount(blowpipe);
        int allowedAmount = MAX_AMOUNT - dartAmount;
        if(allowedAmount == 0) {
            player.sendMessage("The blowpipe can't hold any more darts.");
            return;
        }
        int addAmount = Math.min(allowedAmount, dartItem.getAmount());
        dartItem.incrementAmount(-addAmount);
        update(blowpipe, dart, dartAmount + addAmount, getScalesAmount(blowpipe));
        check(player, blowpipe);
    }

    private static void unload(Player player, Item blowpipe) {
        if(player.isLocked())
            return;
        Dart dart = getDart(blowpipe);
        if(dart == Dart.NONE) {
            player.sendMessage("The blowpipe has no darts in it.");
            return;
        }
        if(player.getInventory().add(dart.id, getDartAmount(blowpipe)) == 0) {
            player.sendMessage("You don't have space to do that.");
            return;
        }
        update(blowpipe, dart, 0, getScalesAmount(blowpipe));
        player.closeDialogue();
    }

    /**
     * Charge/Uncharge
     */

    private static void charge(Player player, Item blowpipe, Item scalesItem) {
        int scalesAmount = getScalesAmount(blowpipe);
        int allowedAmount = MAX_AMOUNT - scalesAmount;
        if (allowedAmount == 0) {
            player.sendMessage("The blowpipe can't hold any more scales.");
            return;
        }
        int addAmount = Math.min(allowedAmount, scalesItem.getAmount());
        scalesItem.incrementAmount(-addAmount);
        update(blowpipe, getDart(blowpipe), getDartAmount(blowpipe), scalesAmount + addAmount);
        check(player, blowpipe);
    }

    private static void uncharge(Player player, Item blowpipe) {
        int reqSlots = 0;
        Dart dart = getDart(blowpipe);
        int dartAmount = getDartAmount(blowpipe);
        if(dartAmount > 0) {
            if(!player.getInventory().hasId(dart.id))
                reqSlots++;
        }
        int scalesAmount = getScalesAmount(blowpipe);
        if(scalesAmount > 0) {
            if(!player.getInventory().hasId(SCALES))
                reqSlots++;
        }
        if(player.getInventory().getFreeSlots() < reqSlots) {
            player.sendMessage("You don't have enough inventory space to uncharge the blowpipe.");
            return;
        }
        player.dialogue(new YesNoDialogue("Are you sure you want to uncharge it?", "If you uncharge the blowpipe, all scales and darts will fall out.", blowpipe, () -> {
            if(player.isLocked())
                return;
            player.lock();
            if(dartAmount > 0)
                player.getInventory().add(dart.id, dartAmount);
            if(scalesAmount > 0)
                player.getInventory().add(SCALES, scalesAmount);
            update(blowpipe, dart, 0, 0);
            player.unlock();
        }));
    }

    public static void update(Item blowpipe, Dart dart, int dartAmount, int scalesAmount) {
        if (dartAmount == 0)
            dart = Dart.NONE;
        int id = (dart != Dart.NONE || scalesAmount > 0) ?  CHARGED : UNCHARGED;
        if(blowpipe.getId() != id)
            blowpipe.setId(id);
        AttributeExtensions.putAttribute(blowpipe, AttributeTypes.AMMO_ID, dart.ordinal());
        AttributeExtensions.putAttribute(blowpipe, AttributeTypes.AMMO_AMOUNT, dartAmount);
        AttributeExtensions.setCharges(blowpipe, scalesAmount);
    }

    public static Dart getDart(Item blowpipe) {
        return Dart.values()[blowpipe.getAttributeInt(AttributeTypes.AMMO_ID, 0)];
    }

    public static int getDartAmount(Item blowpipe) {
        return blowpipe.getAttributeInt(AttributeTypes.AMMO_AMOUNT, 0);
    }

    public static int getScalesAmount(Item blowpipe) {
        return AttributeExtensions.getCharges(blowpipe);
    }

    /**
     * Registering
     */

    static {

        ItemAction.registerInventory(UNCHARGED, "dismantle", Blowpipe::dismantle);
        ItemAction.registerInventory(CHARGED, "check", Blowpipe::check);
        ItemAction.registerEquipment(CHARGED, "check", Blowpipe::check);
        ItemAction.registerInventory(CHARGED, "unload", Blowpipe::unload);
        ItemAction.registerInventory(CHARGED, "uncharge", Blowpipe::uncharge);

        ItemItemAction.register(Tool.CHISEL, TAZANITE_FANG, (player, primary, secondary) -> {
            if(!player.getStats().check(StatType.Fletching, 53, CHISEL, TAZANITE_FANG, "do that"))
                return;
            player.startEvent(event -> {
                secondary.setId(UNCHARGED);
                player.animate(3015);
                player.getStats().addXp(StatType.Fletching, 120.0, true);
                player.dialogue(new ItemDialogue().one(UNCHARGED, "You carve the fang and turn it into a powerful blowpipe."));
            });
        });

        for(Dart dart : Dart.values()) {
            if(dart != Dart.NONE) {
                ItemItemAction loadAction = (player, blowpipe, dartItem) ->
                        Blowpipe.load(player, blowpipe, dartItem, dart);
                ItemItemAction.register(UNCHARGED, dart.id, loadAction);
                ItemItemAction.register(CHARGED, dart.id, loadAction);
            }
        }

        ItemItemAction.register(UNCHARGED, SCALES, Blowpipe::charge);
        ItemItemAction.register(CHARGED, SCALES, Blowpipe::charge);

        ItemItemAction loadPoisonedAction = (player, primary, secondary) ->
                player.sendMessage("You can't use that kind of dart - the venom doesn't mix with other poisons.");
        ItemDef.forEach(def -> {
            if(def.name.toLowerCase().contains("dart(p")) {
                ItemItemAction.register(UNCHARGED, def.id, loadPoisonedAction);
                ItemItemAction.register(CHARGED, def.id, loadPoisonedAction);
            }
        });
    }

}