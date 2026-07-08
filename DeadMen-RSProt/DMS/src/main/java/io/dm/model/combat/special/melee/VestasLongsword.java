package io.dm.model.combat.special.melee;

import io.dm.api.utils.Random;
import io.dm.cache.ItemDef;
import io.dm.model.combat.AttackStyle;
import io.dm.model.combat.AttackType;
import io.dm.model.combat.CombatUtils;
import io.dm.model.combat.Hit;
import io.dm.model.combat.special.Special;
import io.dm.model.entity.Entity;
import io.dm.model.entity.player.Player;

public class VestasLongsword implements Special {

    @Override
    public boolean accept(ItemDef def, String name) {
        return name.equalsIgnoreCase("vesta's longsword");
    }

    @Override
    public boolean handle(Player player, Entity target, AttackStyle attackStyle, AttackType attackType, int maxDamage) {
        player.animate(7515);
        Hit hit = new Hit(player, attackStyle, attackType);
        int sides = (int) CombatUtils.getDefenceBonus(target, hit.attackStyle);
        int chance = (int) (sides * 0.75);
        if (Random.rollDie(sides, chance)) {
            hit.defenceIgnored = true;
            hit.randDamage((int)(maxDamage * 0.20), (int) (maxDamage * 1.20));
        } else {
            hit.block();
        }

        target.hit(hit);
        return true;
    }

    @Override
    public int getDrainAmount() {
        return 25;
    }
}