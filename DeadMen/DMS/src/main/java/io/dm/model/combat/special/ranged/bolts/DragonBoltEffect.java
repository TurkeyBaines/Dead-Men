package io.dm.model.combat.special.ranged.bolts;

import io.dm.api.utils.Random;
import io.dm.model.combat.Hit;
import io.dm.model.entity.Entity;

import java.util.function.BiFunction;

public class DragonBoltEffect implements BiFunction<Entity, Hit, Boolean> {

    @Override
    public Boolean apply(Entity target, Hit hit) {
        if(!Random.rollPercent(6))
            return false;
        if(target.getCombat().getDragonfireResistance() > 0.3)
            return false;
        int damage = target.hit(hit.boostDamage(0.45));
        if(damage > 0)
            target.graphics(756);
        return true;
    }

}