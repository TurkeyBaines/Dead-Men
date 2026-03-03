package io.dm.model.activities.godwars.combat.impl.armadyl;

import io.dm.model.combat.AttackStyle;
import io.dm.model.entity.npc.NPCCombat;
import io.dm.model.map.Projectile;

public class WingmanSkree extends NPCCombat {

    private static final Projectile PROJECTILE = new Projectile(1201, 100, 31, 31, 56, 10, 16, 64);

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
        npc.graphics(1194);
        projectileAttack(PROJECTILE, info.attack_animation, AttackStyle.MAGIC, info.max_damage);
        return true;
    }
}
