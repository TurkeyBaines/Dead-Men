package io.dm.model.skills.magic.spells.ancient;

import io.dm.api.utils.Random;
import io.dm.model.combat.Hit;
import io.dm.model.entity.Entity;
import io.dm.model.skills.magic.spells.TargetSpell;

public class SmokeSpell extends TargetSpell {

    private int poisonDamage;

    public SmokeSpell(int poisonDamage) {
        this.poisonDamage = poisonDamage;
    }

    @Override
    protected void afterHit(Hit hit, Entity target) {
        if(hit.damage > 0 && Random.rollDie(4)) {
            if (hit.attacker.player != null && hit.attacker.player.getEquipment().hasId(22647)) // zuriel's staff
                target.poison(poisonDamage * 2);
            else
                target.poison(poisonDamage);
        }
    }
}
