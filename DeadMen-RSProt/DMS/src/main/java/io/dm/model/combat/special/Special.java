package io.dm.model.combat.special;

import io.dm.api.utils.PackageLoader;
import io.dm.cache.ItemDef;
import io.dm.model.combat.AttackStyle;
import io.dm.model.combat.AttackType;
import io.dm.model.entity.Entity;
import io.dm.model.entity.player.Player;

public interface Special extends ItemDef.ItemDefPredicate {

    default boolean handle(Player player, Entity victim, AttackStyle attackStyle, AttackType attackType, int maxDamage) {
        /* override required */
        return false;
    }

    default boolean handleActivation(Player player) {
        /* override required */
        return false;
    }

    default int getDrainAmount() {
        return 0;
    }

    static void load() throws Exception {
        for(Class c : PackageLoader.load("io.dm.model.combat.special", Special.class)) {
            Special special = (Special) c.newInstance();
            ItemDef.forEach(def -> {
                if(special.test(def))
                    def.special = special;
            });
        }
    }
}