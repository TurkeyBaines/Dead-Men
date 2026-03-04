package io.dm.model.inter.handlers;

import io.dm.model.entity.player.Player;
import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceHandler;
import io.dm.model.inter.actions.DefaultAction;
import io.dm.model.inter.actions.SimpleAction;
import io.dm.model.inter.actions.SlotAction;
import io.dm.model.inter.dialogue.MessageDialogue;
import io.dm.model.inter.dialogue.YesNoDialogue;
import io.dm.model.inter.utils.Config;
import io.dm.model.item.Item;
import io.dm.model.item.actions.impl.PrayerScroll;
import io.dm.model.skills.prayer.Prayer;

import static io.dm.cache.ItemID.COINS_995;

public class TabPrayer {

    static {
        InterfaceHandler.register(Interface.PRAYER, h -> {
            for(Prayer prayer : Prayer.values()) {
                if(prayer == Prayer.RIGOUR) {
                    h.actions[prayer.ordinal() + 5] = (DefaultAction) (p, option, slot, itemId) -> {
                        if(option == 1)
                            p.getPrayer().toggle(prayer);
                        else
                            refundPrayer(p, prayer);
                    };
                } else if(prayer == Prayer.AUGURY) {
                    h.actions[prayer.ordinal() + 5] = (DefaultAction) (p, option, slot, itemId) -> {
                        if(option == 1)
                            p.getPrayer().toggle(prayer);
                        else
                            refundPrayer(p, prayer);
                    };
                } else {
                    h.actions[prayer.ordinal() + 5] = (SimpleAction) p -> p.getPrayer().toggle(prayer);
                }
            }
        });
        InterfaceHandler.register(Interface.QUICK_PRAYER, h -> {
            h.actions[4] = (SlotAction) (p, slot) -> {
                Prayer prayer = Prayer.getQuickPrayer(slot);
                if(prayer != null)
                    p.getPrayer().toggleQuickPrayer(prayer);
            };
            h.actions[5] = (SimpleAction) p -> setupQuickPrayers(p, false);
        });
    }

    public static void refundPrayer(Player player, Prayer prayer) {
        if(prayer == Prayer.AUGURY && Config.AUGURY_UNLOCK.get(player) == 0) {
            player.dialogue(new MessageDialogue("You have to learn how to use <col=000080>Augury</col> before attempting to refund the scroll!"));
            return;
        }

        if(prayer == Prayer.RIGOUR && Config.RIGOUR_UNLOCK.get(player) == 0) {
            player.dialogue(new MessageDialogue("You have to learn how to use <col=000080>Rigour</col> before attempting to refund the scroll!"));
            return;
        }

        if(player.getInventory().isFull()) {
            player.sendMessage("You need at least one free inventory slot to do this.");
            return;
        }

        if(player.isLocked()) {
            player.sendMessage("You're too busy to do this!");
            return;
        }

        if(player.joinedTournament) {
            player.sendMessage("You can't refund your scroll inside the tournament!");
            return;
        }

        int itemId = (prayer == Prayer.AUGURY ? PrayerScroll.AUGURY_SCROLL : PrayerScroll.RIGOUR_SCROLL);
        int cost = 5000000;
        int currencyId = COINS_995;
        String currencyName = "coins";

        player.dialogue(new YesNoDialogue("Are you sure you want to do this?", "Pay " + cost + " " + currencyName + " and get your prayer scroll back?", new Item(itemId), () -> {
            Item bloodMoney = player.getInventory().findItem(currencyId);
            if(bloodMoney == null || bloodMoney.getAmount() < cost) {
                player.sendMessage("You need at least " + cost + " " + currencyName + " to refund your prayer scroll.");
                return;
            }
            player.lock();
            player.getInventory().add(itemId, 1);
            bloodMoney.remove(cost);
            if(prayer == Prayer.AUGURY)
                Config.AUGURY_UNLOCK.set(player, 0);
            else
                Config.RIGOUR_UNLOCK.set(player, 0);
            player.sendMessage("You refund your " + (prayer == Prayer.AUGURY ? "Dexterous" : "Arcane") + " prayer scroll.");
            player.unlock();
        }));
    }

    public static void setupQuickPrayers(Player player, boolean setup) {
        switch(player.getGameFrameId()) {
            case 161:
                if(setup) {
                    player.getPacketSender().sendInterface(77, 161, 73, 1);
                    player.getPacketSender().sendAccessMask(161, 56, -1, -1, 2);
                    player.getPacketSender().sendAccessMask(77, 4, 0, 28, 2);
                    player.getPacketSender().sendClientScript(915, "i", 5);
                } else {
                    player.getPacketSender().sendInterface(541, 161, 73, 1);
                    player.getPacketSender().sendAccessMask(161, 56, -1, -1, 2);
                  //  checkPrayerSwap(player);
                }
                return;
            case 164:
                if(setup) {
                    player.getPacketSender().sendInterface(77, 164, 71, 1);
                    player.getPacketSender().sendAccessMask(164, 53, -1, -1, 2);
                    player.getPacketSender().sendAccessMask(77, 4, 0, 28, 2);
                    player.getPacketSender().sendClientScript(915, "i", 5);
                } else {
                    player.getPacketSender().sendInterface(541, 164, 71, 1);
                    player.getPacketSender().sendAccessMask(164, 53, -1, -1, 2);
                  //  checkPrayerSwap(player);
                }
                return;
            default:
                if(setup) {
                    player.getPacketSender().sendInterface(77, 548, 71, 1);
                    player.getPacketSender().sendAccessMask(548, 52, -1, -1, 2);
                    player.getPacketSender().sendAccessMask(77, 4, 0, 28, 2);
                    player.getPacketSender().sendClientScript(915, "i", 5);
                } else {
                    player.getPacketSender().sendInterface(541, 548, 71, 1);
                    player.getPacketSender().sendAccessMask(548, 52, -1, -1, 2);
                   // checkPrayerSwap(player);
                }
                return;
        }
    }
/*
    private static void checkPrayerSwap(Player player) {
        player.getPacketSender().setAlignment(541, (player.swapMagePrayerPosition ? 31 : 27), 0, 148);
        player.getPacketSender().setAlignment(541, (player.swapMagePrayerPosition ? 27 : 31), 111, 185);
        player.getPacketSender().setAlignment(541, (player.swapRangePrayerPosition ? 26 : 30), 74, 185);
        player.getPacketSender().setAlignment(541, (player.swapRangePrayerPosition ? 30 : 26), 148, 111);
    }*/
}