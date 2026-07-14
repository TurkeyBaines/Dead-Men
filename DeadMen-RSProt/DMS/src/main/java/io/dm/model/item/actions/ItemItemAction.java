package io.dm.model.item.actions;

import io.dm.cache.ItemDef;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;

import java.util.ArrayList;

public interface ItemItemAction {

    void handle(Player player, Item primary, Item secondary);

    /**
     * Default register method - For private use only.
     */

    default void register(int id, boolean primary) {
        ItemDef def = ItemDef.get(id);
        ArrayList<ItemItemAction> list;
        if(primary) {
            if(def.primaryItemItemActions == null)
                def.primaryItemItemActions = new ArrayList<>();
            list = def.primaryItemItemActions;
        } else {
            if(def.secondaryItemItemActions == null)
                def.secondaryItemItemActions = new ArrayList<>();
            list = def.secondaryItemItemActions;
        }
        list.add(this);
        list.trimToSize(); //only okay because it's on startup ;)
    }

    /**
     * Registers actions without a specific secondary item.
     */

    static void register(int primaryId, ItemItemAction action) {
        ItemDef def = ItemDef.get(primaryId);
        def.defaultPrimaryItemItemAction = action;
    }

    /**
     * Registers actions with a specific primary & secondary item.
     */

    static void register(int primaryId, int secondaryId, ItemItemAction action) {
        action.register(primaryId, true);
        action.register(secondaryId, false);
    }

    /**
     * Handle actions
     */

    static void handleAction(Player player, Item from, Item to) {
        if(!tryHandle(player, from, to))
            player.sendMessage("Nothing interesting happens.");
    }

    /**
     * Same lookup/dispatch as {@link #handleAction}, but reports whether a match was actually
     * found instead of always falling back to "Nothing interesting happens." — used by
     * {@code TabInventory.drag} (Stage 05), which needs to try combining two dragged items
     * without spamming that message on every ordinary reorder-drag that isn't a combine attempt.
     */
    static boolean tryHandle(Player player, Item from, Item to) {
        ItemDef fromDef = from.getDef();
        ItemDef toDef = to.getDef();
        ItemItemAction action;
        if((action = match(fromDef.primaryItemItemActions, toDef.secondaryItemItemActions)) != null) {
            action.handle(player, from, to);
            return true;
        }
        if((action = match(toDef.primaryItemItemActions, fromDef.secondaryItemItemActions)) != null) {
            action.handle(player, to, from);
            return true;
        }
        if((action = fromDef.defaultPrimaryItemItemAction) != null) {
            action.handle(player, from, to);
            return true;
        }
        if((action = toDef.defaultPrimaryItemItemAction) != null) {
            action.handle(player, to, from);
            return true;
        }
        return false;
    }

    static ItemItemAction match(ArrayList<ItemItemAction> primaryActions, ArrayList<ItemItemAction> secondaryActions) {
        if(primaryActions == null || secondaryActions == null)
            return null;
        for(ItemItemAction primary : primaryActions) {
            for(ItemItemAction secondary : secondaryActions) {
                if(primary == secondary)
                    return primary;
            }
        }
        return null;
    }

}
