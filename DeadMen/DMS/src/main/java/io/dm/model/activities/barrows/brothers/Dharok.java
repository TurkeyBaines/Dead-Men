package io.dm.model.activities.barrows.brothers;

import io.dm.api.utils.Random;
import io.dm.model.combat.Hit;
import io.dm.model.entity.npc.NPCCombat;

public class Dharok extends NPCCombat {

    @Override
    public void init() {
    }

    @Override
    public void follow() {
        follow(1);
    }

    @Override
    public boolean attack() {
        if(!withinDistance(1))
            return false;
        Hit hit = basicAttack();
        if(Random.rollDie(4))
            hit.boostDamage((npc.getMaxHp() - npc.getHp()) * 0.01);
        return true;
    }

}