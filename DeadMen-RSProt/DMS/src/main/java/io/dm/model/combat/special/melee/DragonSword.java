package io.dm.model.combat.special.melee;

import io.dm.cache.ItemDef;
import io.dm.model.combat.AttackStyle;
import io.dm.model.combat.AttackType;
import io.dm.model.combat.Hit;
import io.dm.model.combat.special.Special;
import io.dm.model.entity.Entity;
import io.dm.model.entity.player.Player;

//Wild Stab: Deal an attack which hits through Protect
//from Melee with 25% increased accuracy and damage. (40%)
public class DragonSword implements Special {

    @Override
    public boolean accept(ItemDef def, String name) {
        return name.contains("dragon sword");
    }

    @Override
    public boolean handle(Player player, Entity target, AttackStyle attackStyle, AttackType attackType, int maxDamage) {
        player.animate(7515);
        player.graphics(1369, 96, 0);
        target.hit(new Hit(player, attackStyle, attackType).randDamage(maxDamage).boostDamage(0.25).boostAttack(0.25).ignorePrayer());
        return true;
    }

    @Override
    public int getDrainAmount() {
        return 40;
    }

}