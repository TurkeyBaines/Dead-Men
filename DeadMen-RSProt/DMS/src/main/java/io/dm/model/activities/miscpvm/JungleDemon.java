package io.dm.model.activities.miscpvm;

import io.dm.api.utils.Random;
import io.dm.model.combat.AttackStyle;
import io.dm.model.combat.Hit;
import io.dm.model.entity.npc.NPCCombat;
import io.dm.model.map.Projectile;

public class JungleDemon extends NPCCombat {

    private static final Projectile PROJECTILE = new Projectile(1462, 75, 31, 28, 56, 8, 16, 64);

    @Override
    public void init() {

    }

    @Override
    public void follow() {
        follow(1);
    }

    @Override
    public boolean attack() {
        if (!withinDistance(10))
            return false;
        if (withinDistance(2) && Random.rollDie(3,2)) {
            meleeAttack();
        } else {
            magicAttack();
        }
        return true;
    }

    private void magicAttack() {
        Hit hit = projectileAttack(PROJECTILE, 69, AttackStyle.MAGIC, info.max_damage);
        if (hit.damage > 0 && Random.rollDie(5, 2)) {
            target.poison(9);
        }
        hit.postDamage(entity -> entity.graphics(1463, 124, 0));
    }

    private void meleeAttack() {
        Hit hit = basicAttack();
        if (hit.damage > 0 && Random.rollDie(5, 2)) {
            target.poison(9);
        }
    }
}
