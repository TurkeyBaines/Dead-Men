package io.dm.model.activities.wilderness.bosses.scorpia;

import io.dm.api.utils.Random;
import io.dm.model.entity.npc.NPCCombat;
import io.dm.model.map.Projectile;

public class ScorpiasOffspring extends NPCCombat {

    public static final Projectile PROJECTILE = new Projectile(663, 6, 16, 20, 30, 0, 18, 96);

    @Override
    public void init() {

    }

    @Override
    public void follow() {
        follow(4);
    }

    @Override
    public boolean attack() {
        if (!withinDistance(4))
            return false;
        int damage = projectileAttack(PROJECTILE, info.attack_animation, info.attack_style, info.max_damage).damage;
        if (Random.rollDie(8, 1))
            target.poison(6);
        if (damage > 0 && target.player != null)
            target.player.getPrayer().drain(Random.get(1, 3));
        return true;
    }
}
