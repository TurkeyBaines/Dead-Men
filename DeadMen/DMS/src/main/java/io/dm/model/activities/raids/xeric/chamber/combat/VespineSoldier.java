package io.dm.model.activities.raids.xeric.chamber.combat;

import io.dm.model.combat.AttackStyle;
import io.dm.model.combat.Hit;
import io.dm.model.entity.npc.NPCCombat;
import io.dm.model.map.Projectile;
import io.dm.model.skills.prayer.Prayer;

public class VespineSoldier extends NPCCombat {

    private static final Projectile PROJECTILE = new Projectile(1486, 40, 43, 30, 50, 5, 30, 0);


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
        npc.animate(getInfo().attack_animation);
        int delay = PROJECTILE.send(npc, target);
        target.hit(new Hit(npc, AttackStyle.RANGED).randDamage(target.player.getPrayer().isActive(Prayer.PROTECT_FROM_MISSILES) ? getInfo().max_damage / 2 : getInfo().max_damage).ignorePrayer().clientDelay(delay));
        return true;
    }
}
