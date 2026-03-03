package io.dm.model.inter.handlers;

import io.dm.model.activities.duelarena.DuelArena;
import io.dm.model.entity.Entity;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceAction;
import io.dm.model.inter.InterfaceHandler;
import io.dm.model.inter.actions.DefaultAction;
import io.dm.model.inter.utils.Config;
import io.dm.model.item.Item;
import io.dm.model.map.object.GameObject;
import io.dm.model.skills.magic.Spell;
import io.dm.model.skills.magic.SpellBook;

public class TabMagic {

    static {
        InterfaceHandler.register(Interface.MAGIC_BOOK, h -> {
            for(SpellBook book : SpellBook.values()) {
                for(int i = 0; i < book.spells.length; i++)
                    h.actions[book.spellIdOffset + i] = createAction(book, book.spells[i]);
            }
            h.actions[184] = (DefaultAction) (player, option, slot, itemId) -> {
                if(slot == 0)
                    Config.SHOW_COMBAT_SPELLS.toggle(player);
                if(slot == 1)
                    Config.SHOW_TELEPORT_SPELLS.toggle(player);
                if(slot == 2)
                    Config.SHOW_UTILITY_SPELLS.toggle(player);
                if(slot == 3)
                    Config.SHOW_SPELLS_LACK_LEVEL.toggle(player);
                if(slot == 4)
                    Config.SHOW_SPELLS_LACK_RUNES.toggle(player);
            };
        });
    }

    private static InterfaceAction createAction(SpellBook book, Spell spell) {
        return new InterfaceAction() {
            @Override
            public void handleClick(Player player, int option, int slot, int itemId) {
                if(spell.clickAction == null || !book.isActive(player) && !DuelArena.allowMagic(player))
                    return;
                spell.clickAction.accept(player, option - 1);
            }
            @Override
            public void handleOnInterface(Player player, int fromSlot, int fromItemId, int toInterfaceId, int toChildId, int toSlot, int toItemId) {
                if(spell.itemAction == null || !book.isActive(player) && !DuelArena.allowMagic(player))
                    return;
                Item item = player.getInventory().get(toSlot, toItemId);
                if(item == null)
                    return;
                spell.itemAction.accept(player, item);
            }
            @Override
            public void handleOnEntity(Player player, Entity entity, int slot, int itemId) {
                if(spell.entityAction == null || !book.isActive(player) && !DuelArena.allowMagic(player))
                    return;
                spell.entityAction.accept(player, entity);
            }
            @Override
            public void handleOnObject(Player player, int slot, int itemId, GameObject obj) {
                if(spell.objectAction == null || !book.isActive(player) && !DuelArena.allowMagic(player))
                    return;
                spell.objectAction.accept(player, obj);
            }
        };
    }

}