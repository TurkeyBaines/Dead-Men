package io.dm.model.activities.barrows.brothers;

import io.dm.api.utils.Random;
import io.dm.model.combat.AttackStyle;
import io.dm.model.entity.npc.NPCCombat;
import io.dm.model.map.Projectile;
import io.dm.model.stat.StatType;

public class Karil extends NPCCombat {

    @Override
    public void init() {
    }

    @Override
    public void follow() {
        follow(8);
    }

    @Override
    public boolean attack() {
        if(!withinDistance(8))
            return false;
        projectileAttack(Projectile.BOLT, info.attack_animation, AttackStyle.RANGED, info.max_damage);
        if(Random.rollDie(4)) {
            target.graphics(401, 100, 0);
            target.player.getStats().get(StatType.Magic).drain(5);
        }
        return true;
    }

}