package io.dm.model.inter.handlers;

import io.dm.model.entity.player.Player;
import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceHandler;
import io.dm.model.inter.actions.OptionAction;
import io.dm.model.inter.actions.SimpleAction;
import io.dm.model.item.Item;
import io.dm.model.item.actions.ItemAction;
import io.dm.model.item.containers.Equipment;

public class TabEquipment {

    static {
        // Stage 06 fix (2026-07-11): the ten worn-slot childIds (15-24) used to hold all 11 equipment
        // types, so h.actions[24] was assigned twice (RING then AMMO silently overwrote it) - RING never
        // had a working handler. PROTOCOL_DUMP.txt from live testing shows a real, repeated (5x) click on
        // component=25 that matches no registered handler here, while components 15-22 arrived correctly
        // matching HAT..HANDS below - confirming the real client uses childId 25 for the 11th slot (one
        // past interface 387's legacy/Cache228-reported "25 components", the same undercount class as
        // interface 548's fixed-frame panes). Split RING=24/AMMO=25 and register with 26 slots of
        // capacity (see InterfaceHandler#register(int,int,Consumer)) so childId 25 doesn't throw.
        InterfaceHandler.register(Interface.EQUIPMENT, 26, h -> {
            h.actions[15] = (OptionAction) (player, option) -> itemAction(player, option, Equipment.SLOT_HAT);
            h.actions[16] = (OptionAction) (player, option) -> itemAction(player, option, Equipment.SLOT_CAPE);
            h.actions[17] = (OptionAction) (player, option) -> itemAction(player, option, Equipment.SLOT_AMULET);
            h.actions[18] = (OptionAction) (player, option) -> itemAction(player, option, Equipment.SLOT_WEAPON);
            h.actions[19] = (OptionAction) (player, option) -> itemAction(player, option, Equipment.SLOT_CHEST);
            h.actions[20] = (OptionAction) (player, option) -> itemAction(player, option, Equipment.SLOT_SHIELD);
            h.actions[21] = (OptionAction) (player, option) -> itemAction(player, option, Equipment.SLOT_LEGS);
            h.actions[22] = (OptionAction) (player, option) -> itemAction(player, option, Equipment.SLOT_HANDS);
            h.actions[23] = (OptionAction) (player, option) -> itemAction(player, option, Equipment.SLOT_FEET);
            h.actions[24] = (OptionAction) (player, option) -> itemAction(player, option, Equipment.SLOT_RING);
            h.actions[25] = (OptionAction) (player, option) -> itemAction(player, option, Equipment.SLOT_AMMO);
            h.actions[1] = (SimpleAction) EquipmentStats::open;
            //l0l oops h.actions[19] = (SimpleAction) p -> p.sendMessage("This feature will be added with the release of the Grand Exchange!");
            h.actions[5] = (SimpleAction) IKOD::open;
            h.actions[7] = (SimpleAction) p -> {
                if(p.pet == null)
                    p.sendMessage("You don't have a follower.");
                else
                    p.callPet = true;
            };
        });
    }

    public static void itemAction(Player player, int option, int slot) {
        if(player.isLocked())
            return;
        Item item = player.getEquipment().get(slot);
        if(item == null)
            return;
        if(option == 10) {
            item.examine(player);
            return;
        }
        int i = option - 1;
        if(i < 0 || i >= 6)
            return;
        ItemAction[] actions = item.getDef().equipmentActions;
        if(actions != null) {
            ItemAction action = actions[i];
            if(action != null) {
                action.handle(player, item);
                return;
            }
        }
        if(option == 1) {
            player.closeChatbox(false);
            player.getEquipment().unequip(item);
        }
    }

}