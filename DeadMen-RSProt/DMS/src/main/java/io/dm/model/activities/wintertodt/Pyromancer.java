package io.dm.model.activities.wintertodt;

import io.dm.model.combat.Hit;
import io.dm.model.entity.Entity;
import io.dm.model.entity.npc.NPCCombat;

public class Pyromancer extends NPCCombat {

    @Override
    public void startDeath(Hit killHit) {
        npc.transform(Wintertodt.INCAPACITATED_PYROMANCER);
    }

    @Override
    public void init() {
    }

    @Override
    public void follow() {
    }

    @Override
    public boolean attack() {
        return false;
    }

    @Override
    public boolean allowRetaliate(Entity attacker) {
        return false;
    }

}
