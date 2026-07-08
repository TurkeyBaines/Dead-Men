package io.dm.model.activities.fightcaves.monsters;

import io.dm.api.utils.Random;
import io.dm.model.combat.AttackStyle;
import io.dm.model.entity.npc.NPCCombat;
import io.dm.model.map.Projectile;

public class KetZek extends NPCCombat {

    private static final Projectile MAGIC_PROJECTILE = new Projectile(445, 128, 31, 28, 36, 8, 16, 32).regionBased();

    @Override
    public void init() {
    }

    @Override
    public void follow() {
        follow(16);
    }

    @Override
    public boolean attack() {
        if(withinDistance(1)) {
            if(Random.rollDie(3)) {
                basicAttack(info.attack_animation, info.attack_style, info.max_damage);
                return true;
            }
        } else if(!withinDistance(16)) {
            /**
             * Not in ranged distance
             */
            return false;
        }
        projectileAttack(MAGIC_PROJECTILE, 2647, AttackStyle.MAGIC, info.max_damage);
        return true;
    }

}