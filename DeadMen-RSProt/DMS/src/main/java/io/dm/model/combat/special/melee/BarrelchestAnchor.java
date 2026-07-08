package io.dm.model.combat.special.melee;

import io.dm.api.utils.Random;
import io.dm.cache.ItemDef;
import io.dm.model.combat.AttackStyle;
import io.dm.model.combat.AttackType;
import io.dm.model.combat.Hit;
import io.dm.model.combat.special.Special;
import io.dm.model.entity.Entity;
import io.dm.model.entity.player.Player;
import io.dm.model.stat.StatType;

//Sunder: Deal an attack with double accuracy and 10% higher max hit. Lowers your
//target's Attack, Defence, Ranged or Magic level by 10% of the damage dealt. (50%)
public class BarrelchestAnchor implements Special {

    private static final StatType[] DRAIN_STATS = {StatType.Attack, StatType.Defence, StatType.Ranged, StatType.Magic};

    @Override
    public boolean accept(ItemDef def, String name) {
        return def.id == 10887;
    }

    @Override
    public boolean handle(Player player, Entity target, AttackStyle attackStyle, AttackType attackType, int maxDamage) {
        player.animate(6147);
        player.graphics(1027);
        int damage = target.hit(new Hit(player, attackStyle, attackType)
                .randDamage(maxDamage)
                .boostAttack(1.0)
                .boostDamage(0.10)
        );
        if(damage > 0 && target.player != null)
            target.player.getStats().get(Random.get(DRAIN_STATS)).drain((int) (damage * 0.10));
        return true;
    }

    @Override
    public int getDrainAmount() {
        return 50;
    }
}
