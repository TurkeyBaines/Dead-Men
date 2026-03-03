package io.dm.model.activities.miscpvm;

import io.dm.model.entity.Entity;
import io.dm.model.entity.npc.NPCCombat;

public class PassiveCombat extends NPCCombat {

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