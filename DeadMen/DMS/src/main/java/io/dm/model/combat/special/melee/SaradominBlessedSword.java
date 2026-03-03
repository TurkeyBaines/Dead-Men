package io.dm.model.combat.special.melee;

import io.dm.cache.ItemDef;
import io.dm.model.combat.AttackStyle;
import io.dm.model.combat.AttackType;
import io.dm.model.combat.Hit;
import io.dm.model.combat.special.Special;
import io.dm.model.entity.Entity;
import io.dm.model.entity.player.Player;

//Saradomin's Lightning: Call upon Saradomin's power
//to perform an attack with 25% higher max hit. (65%)
public class SaradominBlessedSword implements Special {

    @Override
    public boolean accept(ItemDef def, String name) {
        return name.contains("blessed sword");
    }

    @Override
    public boolean handle(Player player, Entity target, AttackStyle attackStyle, AttackType attackType, int maxDamage) {
        player.animate(1132);
        player.graphics(1213, 0, 0);
        target.graphics(1196, 30, 0);
        player.publicSound(2537);
        target.hit(new Hit(player, AttackStyle.MAGICAL_MELEE, attackType).randDamage(maxDamage).boostDamage(0.25));
        return true;
    }

    @Override
    public int getDrainAmount() {
        return 65;
    }

}