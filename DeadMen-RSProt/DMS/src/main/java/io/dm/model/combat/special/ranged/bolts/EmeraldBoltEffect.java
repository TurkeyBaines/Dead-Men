package io.dm.model.combat.special.ranged.bolts;

import io.dm.api.utils.Random;
import io.dm.model.combat.Hit;
import io.dm.model.entity.Entity;

import java.util.function.BiFunction;

public class EmeraldBoltEffect implements BiFunction<Entity, Hit, Boolean> {

    @Override
    public Boolean apply(Entity target, Hit hit) {
        if(!Random.rollPercent(target.player != null ? 54 : 55))
            return false;
        target.poison(5);
        target.graphics(752);
        target.hit(hit);
        return true;
    }

}