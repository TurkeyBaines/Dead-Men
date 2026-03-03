package io.dm.model.skills.magic.spells.modern;

import io.dm.model.combat.Hit;
import io.dm.model.entity.Entity;
import io.dm.model.item.actions.impl.chargable.TomeOfFire;
import io.dm.model.skills.magic.spells.TargetSpell;

public class FireSpell extends TargetSpell {

    @Override
    protected void beforeHit(Hit hit, Entity target) {
        super.beforeHit(hit, target);
        if (hit.attacker != null && hit.attacker.player != null) {
            //Tome of Fire Damage Boost
            if (TomeOfFire.consumeCharge(hit.attacker.player)) {
                hit.boostDamage(0.5);
            }
            //Ice Demon Fire Spell Damage Boost
            if (target.npc != null && target.npc.getId() == 7585) {
                hit.boostDamage(1.5);
                hit.ignoreDefence();
            }
        }
    }
}
