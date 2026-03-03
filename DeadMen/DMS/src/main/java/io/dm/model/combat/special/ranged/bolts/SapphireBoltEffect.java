package io.dm.model.combat.special.ranged.bolts;

import io.dm.api.utils.Random;
import io.dm.model.combat.Hit;
import io.dm.model.entity.Entity;
import io.dm.model.stat.Stat;
import io.dm.model.stat.StatType;

import java.util.function.BiFunction;

public class SapphireBoltEffect implements BiFunction<Entity, Hit, Boolean> {

    @Override
    public Boolean apply(Entity target, Hit hit) {
        if(target.player == null || !Random.rollPercent(5))
            return false;
        Stat prayer = hit.attacker.player.getStats().get(StatType.Prayer);
        int drain = prayer.currentLevel / 20;
        if(drain == 0)
            return false;
        prayer.boost(drain / 2, 0.0);
        target.player.getPrayer().drain(drain);
        target.graphics(751);
        target.hit(hit);
        return true;
    }

}