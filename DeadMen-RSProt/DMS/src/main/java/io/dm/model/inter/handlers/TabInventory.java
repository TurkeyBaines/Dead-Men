package io.dm.model.inter.handlers;

import io.dm.cache.Cache228ItemDefs;
import io.dm.cache.ItemDef;
import io.dm.model.activities.wilderness.BloodyChest;
import io.dm.model.entity.Entity;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceAction;
import io.dm.model.inter.InterfaceHandler;
import io.dm.model.item.Item;
import io.dm.model.item.actions.*;
import io.dm.model.map.Tile;
import io.dm.model.map.ground.GroundItem;
import io.dm.model.map.object.GameObject;
import io.dm.services.Loggers;

public class TabInventory {

    static {
        InterfaceHandler.register(Interface.INVENTORY, h -> {
            h.actions[0] = new InterfaceAction() {
                @Override
                public void handleClick(Player player, int option, int slot, int itemId) {
                    click(player, option, slot, itemId);
                }
                @Override
                public void handleDrag(Player player, int fromSlot, int fromItemId, int toInterfaceId, int toChildId, int toSlot, int toItemId) {
                    drag(player, fromSlot, toSlot);
                }
                @Override
                public void handleOnInterface(Player player, int fromSlot, int fromItemId, int toInterfaceId, int toChildId, int toSlot, int toItemId) {
                    onInterface(player, fromSlot, fromItemId, toSlot, toItemId);
                }
                @Override
                public void handleOnGroundItem(Player player, int slot, int itemId, GroundItem groundItem, int distance) {
                    onGroundItem(player, slot, itemId, groundItem, distance);
                }
                @Override
                public void handleOnEntity(Player player, Entity entity, int slot, int itemId) {
                    onEntity(player, entity, slot, itemId);
                }
                @Override
                public void handleOnObject(Player player, int slot, int itemId, GameObject obj) {
                    onObject(player, slot, itemId, obj);
                }
            };
        });
    }

    private static void click(Player player, int option, int slot, int itemId) {
        if(player.isLocked()) {
            io.dm.network.logging.NetworkLogger.logItemOptionMismatch(player, itemId, option, -1, -1, null, "locked");
            return;
        }
        Item item = player.getInventory().get(slot, itemId);
        if(item == null) {
            io.dm.network.logging.NetworkLogger.logItemOptionMismatch(player, itemId, option, -1, -1, null, "slot/id mismatch");
            return;
        }
        if(option == 10) {
            item.examine(player);
            return;
        }
        if(option == 9) {
            // "Use" is a fixed, always-available client op on EVERY item in real OSRS, regardless
            // of per-item cache text — confirmed via live client screenshots (Wield/Eat/Use/Drop/
            // Examine consistently, Use always immediately before Drop) — same idea as Examine
            // being hardcoded at op=10 regardless of array content. Stage 05 previously flagged
            // op 9 as "unobserved/reserved" simply because nobody had clicked Use to find out.
            // Nothing needs to happen server-side for this click itself: the client enters its own
            // local "selected for targeting" state, and the actual item-on-X interaction fires via
            // the already-wired OpLocT/OpNpcT/OpObjT/OpPlayerT/IfButtonT handlers (see
            // stages/STAGE-08) once a target is clicked. Previously fell through to the bottom
            // catch-all and misfired as Examine.
            return;
        }
        ItemDef def = item.getDef();

        // The real 228 client's op numbers for inventory options do NOT match the legacy "raw
        // index + 1" assumption (184-era), and — contrary to the initial hypothesis — it's also
        // NOT about cache content: Cache228 and the legacy cache report identical inventoryOptions
        // for every item tested. Confirmed live on two items (2026-07-08): whip (opts=[null,Wield,
        // null,null,Drop]: Wield idx1->op3, Drop idx4->op7) and Armadyl godsword (opts=[null,Wield,
        // Dismantle,null,Drop]: Wield idx1->op3, Dismantle idx2->op4, Drop idx4->op7 — SAME op7 as
        // the whip's Drop despite an extra populated slot before it, ruling out a content-dependent
        // formula). Fits purely positionally: raw slots 0-2 -> op = index+2, slot 4 (always "Drop"
        // by convention) is a fixed op=7 — same idea as Examine already being fixed at op=10
        // regardless of array length. Index 3 was flagged UNCONFIRMED here for a long time (never
        // populated in a tested item) until the sigil items (26002/26003, Sigil of the Meticulous
        // Mage) finally exercised it live (2026-07-17): opts=[null,null,Inspect,Unattune,Destroy] —
        // "Unattune" at raw idx3 sent op=6, NOT op=5 as the idx+2 formula would predict (see
        // DMS/PROTOCOL_DUMP.txt ~18:01, receivedOption=6). idx1/idx2/idx4 all still check out
        // exactly as before, so op=5 is apparently reserved/skipped entirely (same class as the
        // already-reserved 1/8) and idx3 jumps to op=6 instead. See
        // stages/STAGE-05-inventory-actions.txt and HANDOFF.txt's "STAGE 05" section. This also
        // explains the "Wield dismantled the item" bug: the OLD actions[] lookup used raw
        // `option-1` as the array index uncorrected, so option=3 (really Wield, raw index 1)
        // collided with whatever sat at raw index 2 (Dismantle) purely by coincidence of the
        // numbers. resolveRawIndex() below is now used consistently for actions[]/drop/equip so
        // that coincidence can't happen again.
        int rawIndex = resolveRawIndex(option);

        ItemDef real = Cache228ItemDefs.get(itemId);
        String[] opts = real != null ? real.inventoryOptions : def.inventoryOptions;
        int dropIndex = (real != null ? real : def).getOption("drop") - 1;
        int equipIndex = (real != null ? real : def).getOption("wield", "equip", "wear", "ride", "hold") - 1;

        player.sendFilteredMessage("ItemID=" + itemId + " selected=" + option + " rawIndex=" + rawIndex
                + " opts=" + java.util.Arrays.toString(opts) + " dropIndex=" + dropIndex + " equipIndex=" + equipIndex);

        if(rawIndex >= 0 && rawIndex < opts.length && opts[rawIndex] != null) {
            ItemAction[] actions = def.inventoryActions;
            if(actions != null) {
                ItemAction action = actions[rawIndex];
                if(action != null) {
                    action.handle(player, item);
                    return;
                }
            }
            // A real, named option (e.g. "Use") was clicked, but no ItemAction is registered for
            // it yet. Drop/Wield are ALSO never in actions[] by design (they're matched generically
            // by option TEXT via dropIndex/equipIndex below, not a per-item custom action), so this
            // block must let those two fall through instead of stopping here — an unconditional
            // return was tried and immediately broke Drop/Wield for every item (regression reported
            // live 2026-07-09, fixed here by only stopping when rawIndex is neither built-in slot).
            // Falling through for anything else would misfire in the common case where THIS item
            // also happens to have Drop/Wield at another slot, and falling all the way to the
            // bottom's examine() fallback would misrepresent this click as "Examine" (confirmed
            // live: clicking "Use" on item 5606 incorrectly examined it). "Use"'s real item-on-X
            // targeting flow is client-side + the already-wired OpLocT/OpNpcT/OpObjT/OpPlayerT/
            // IfButtonT handlers (see stages/STAGE-08) — nothing needs to happen server-side for
            // the initial click itself.
            if(rawIndex != dropIndex && rawIndex != equipIndex) {
                return;
            }
        }
        if(rawIndex >= 0 && rawIndex == dropIndex) {
            player.sendFilteredMessage("[DEBUG] Drop Option Reached!");
            Tile tile = player.getPosition().getTile();
            if(tile != null && !tile.allowDrop) {
                player.sendMessage("You can't drop items here.");
                return;
            }
            if (player.jailOresAssigned > 0) {
                player.sendMessage("You cannot drop items while in jail.");
                return;
            }
            if(player.joinedTournament) {
                player.sendMessage("You can't drop items while you're signed up for a tournament.");
                return;
            }
            if (player.isInOwnHouse() && player.getCurrentHouse().isBuildingMode()) {
                player.sendMessage("You can't drop items while in building mode.");
                return;
            }
            if(player.supplyChestRestricted) {
                player.sendMessage("The power of the supply chest prevents you from dropping items!");
                return;
            }
            if(player.wildernessLevel > 0 && BloodyChest.hasBloodyKey(player)) {
                player.sendMessage("The power of your bloody key prevents you from dropping items!");
            }
            item.remove();
            player.resetActions(true, false, true);
            player.privateSound(2739);
            if(player.wildernessLevel > 0) {
                /**
                 * Dropping in the wilderness
                 */
                if(def.consumable || !def.tradeable)
                    new GroundItem(item).owner(player).droppedBy(player).position(player.getPosition()).spawnPrivate();
                else
                    new GroundItem(item).owner(player).droppedBy(player).position(player.getPosition()).spawnPublic();
            } else {
                /**
                 * Player is in a raid, so make items appear instantly.
                 */
                if (player.raidsParty != null) {
                    new GroundItem(item).owner(player).droppedBy(player).position(player.getPosition()).spawnPublic();
                } else {
                    /**
                     * Regular dropping
                     */
                    new GroundItem(item).owner(player).droppedBy(player).position(player.getPosition()).spawn();
                }
            }
            Loggers.logDrop(player.getUserId(), player.getName(), player.getIp(), item.getId(), item.getAmount(), player.getAbsX(), player.getAbsY(), player.getHeight());
            return;
        }
        if (rawIndex >= 0 && rawIndex == equipIndex) {
            player.sendMessage("[DEBUG] Equip Option Reached!");
            player.getEquipment().equip(item);
            player.resetActions(false, player.getMovement().following != null, true);
            return;
        }
        io.dm.network.logging.NetworkLogger.logItemOptionMismatch(player, itemId, option, dropIndex, equipIndex, opts);
        item.examine(player);
    }

    /**
     * Reverses the real 228 client's op-numbering (see the comment in {@link #click}) back to a
     * raw {@code inventoryOptions} array index: slots 0-2 use {@code op = index + 2}, slot 3 is a
     * fixed {@code op = 6}, and slot 4 (conventionally "Drop") is a fixed {@code op = 7}. Returns
     * -1 for any op that doesn't map to a known array slot — either genuinely unobserved/reserved
     * (1, 5, 8), or a fixed op handled separately before this is ever called: 9 is "Use" (always
     * available on every item, independent of cache text) and 10 is Examine.
     */
    private static int resolveRawIndex(int option) {
        if(option == 7)
            return 4;
        if(option == 6)
            return 3;
        int index = option - 2;
        return index >= 0 && index <= 2 ? index : -1;
    }

    public static void drag(Player player, int fromSlot, int toSlot) {
        if(fromSlot == toSlot)
            return;
        if(!player.getInventory().validateSlots(fromSlot, toSlot)) {
            return;
        }
        Item fromItem = player.getInventory().get(fromSlot);
        if(fromItem == null) {
            player.getInventory().update(toSlot);
            player.getInventory().update(fromSlot);
            return;
        }
        player.closeChatbox(false);
        Item toItem = player.getInventory().get(toSlot);
        if(toItem == null) {
            player.getInventory().set(fromSlot, null);
            player.getInventory().set(toSlot, fromItem);
            return;
        }
        // Stage 05 Round 9 (2026-07-11) previously had this call ItemItemAction.tryHandle() before
        // swapping, on the theory that the real client represents same-inventory item-on-item as a
        // drag rather than an IfButtonT. Reverted in Round 11: human confirmed real OSRS does NOT
        // combine on an ordinary reorder-drag — dragging one item onto another must always just
        // swap/reorder, never trigger a combine as a side effect. Combining belongs solely to the
        // "Use" + click flow (IfButtonT -> TabInventory.onInterface), which is the actual bug still
        // being chased — see stages/STAGE-05's Round 11 notes.
        player.getInventory().set(toSlot, fromItem);
        player.getInventory().set(fromSlot, toItem);
    }

    private static void onInterface(Player player, int fromSlot, int fromItemId, int toSlot, int toItemId) {
        Item fromItem = player.getInventory().get(fromSlot, fromItemId);
        if(fromItem == null)
            return;
        Item toItem = player.getInventory().get(toSlot, toItemId);
        if(toItem == null)
            return;
        ItemItemAction.handleAction(player, fromItem, toItem);
    }

    private static void onGroundItem(Player player, int slot, int itemId, GroundItem groundItem, int distance) {
        Item item = player.getInventory().get(slot, itemId);
        if(item == null)
            return;
        ItemGroundItemAction.handleAction(player, item, groundItem, distance);
    }

    private static void onEntity(Player player, Entity entity, int slot, int itemId) {
        Item item = player.getInventory().get(slot, itemId);
        if(item == null)
            return;
        if(entity.npc != null)
            ItemNPCAction.handleAction(player, item, entity.npc);
        else
            ItemPlayerAction.handleAction(player, item, entity.player);
    }

    private static void onObject(Player player, int slot, int itemId, GameObject obj) {
        Item item = player.getInventory().get(slot, itemId);
        if(item == null)
            return;
        ItemObjectAction.handleAction(player, item, obj);
    }

}