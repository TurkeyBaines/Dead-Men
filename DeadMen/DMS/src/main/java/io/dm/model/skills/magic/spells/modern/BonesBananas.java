package io.dm.model.skills.magic.spells.modern;

import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.item.actions.ItemAction;
import io.dm.model.skills.magic.Spell;
import io.dm.model.skills.magic.rune.Rune;

public class BonesBananas extends Spell {

    public static final Item[] RUNES = new Item[]{
            Rune.NATURE.toItem(1),
            Rune.WATER.toItem(2),
            Rune.EARTH.toItem(2)
    };
    public static final int LVL_REQ = 15;
    public static final int XP = 25;

    public BonesBananas() {
        registerClick(LVL_REQ, XP, true, RUNES, BonesBananas::cast);
    }

    public static boolean cast(Player p, Integer i) {
        int count = 0;
        for(Item item : p.getInventory().getItems()) {
            if(item != null && item.getDef().allowFruit) {
                item.setId(1963);
                count++;
            }
        }
        if(count > 0) {
            p.animate(722);
            p.graphics(141, 92, 0);
            return true;
        }
        p.sendMessage("You don't have any bones in your inventory to turn to bananas.");
        return false;
    }

    static {
        ItemAction.registerInventory(8014, 1, (player, item) -> {
            if (cast(player, 0)) {
                item.incrementAmount(-1);
            }
        });
    }
}