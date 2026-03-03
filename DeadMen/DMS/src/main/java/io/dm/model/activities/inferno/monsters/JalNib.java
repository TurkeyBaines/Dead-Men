package io.dm.model.activities.inferno.monsters;

import io.dm.model.activities.miscpvm.BasicCombat;
import io.dm.model.entity.Entity;
import io.dm.utility.Misc;

//Pillar attackers (32)
public class JalNib extends BasicCombat {

    @Override
    public boolean attack() {
        if(target.npc != null) {
            int centerX = target.getAbsX() + 1;
            int centerY = target.getAbsY() + 1;
            int distance = Misc.getDistance(npc.getPosition(), centerX, centerY);
            if(distance <= 2) {
                basicAttack();
                return true;
            }
            return false;
        }
        return super.attack();
    }

    @Override
    public boolean allowRetaliate(Entity attacker) {
        return false;
    }

}