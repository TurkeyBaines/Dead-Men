package io.dm.model.skills.magic.spells.lunar;

import io.dm.model.item.Item;
import io.dm.model.skills.magic.Spell;
import io.dm.model.skills.magic.rune.Rune;

public class MagicImbue extends Spell {

    public MagicImbue() {
        Item[] runes = {
                Rune.ASTRAL.toItem(2),
                Rune.FIRE.toItem(7),
                Rune.WATER.toItem(7)
        };
        registerClick(82, 86, true, runes, (p, i) -> {
            if(p.magicImbueEffect.isDelayed()) {
                p.sendFilteredMessage("You are already imbued.");
                return false;
            }

            p.privateSound(2888);
            p.animate(722);
            p.graphics(141, 92, 0);
            p.magicImbueEffect.delay(15);
            p.sendFilteredMessage("You are charged to combine runes!");
            return true;
        });
    }

}