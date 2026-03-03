package io.dm.model.skills.magic.spells.ancient;

import io.dm.model.combat.Hit;
import io.dm.model.entity.Entity;
import io.dm.model.skills.magic.spells.TargetSpell;

public abstract class BloodSpell extends TargetSpell {

    @Override
    protected void afterHit(Hit hit, Entity target) {
        if(hit.damage > 0) {
            int healAmount = hit.damage / 4;
            if (hit.attacker.player != null && hit.attacker.player.getEquipment().hasId(22647)) // zuriel's staff
                healAmount *= 1.5;
            hit.attacker.incrementHp(healAmount);
        }
    }

}