package io.dm.model.activities.godwars.combat.impl.armadyl;

import io.dm.model.combat.AttackStyle;
import io.dm.model.entity.npc.NPCCombat;
import io.dm.model.map.Projectile;

public class FlockleaderGeerin extends NPCCombat {

    private static final Projectile PROJECTILE = new Projectile(1192, 62, 36, 41, 51, 5, 15, 64);

    @Override
    public void init() {

    }

    @Override
    public void follow() {
        follow(8);
    }

    @Override
    public boolean attack() {
        if (!withinDistance(8))
            return false;
        projectileAttack(PROJECTILE, info.attack_animation, AttackStyle.RANGED, info.max_damage);
        return true;
    }
}
