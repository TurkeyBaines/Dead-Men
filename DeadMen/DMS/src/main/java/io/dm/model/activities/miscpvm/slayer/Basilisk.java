package io.dm.model.activities.miscpvm.slayer;

import io.dm.model.combat.Hit;
import io.dm.model.entity.npc.NPCCombat;
import io.dm.model.item.containers.Equipment;
import io.dm.model.stat.StatType;

public class Basilisk extends NPCCombat {

    private static StatType[] DRAIN = { StatType.Attack, StatType.Strength, StatType.Defence, StatType.Ranged, StatType.Magic };

    @Override
    public void init() {

    }

    @Override
    public void follow() {
        follow(1);
    }

    @Override
    public boolean attack() {
        if (!withinDistance(8))
            return false;
        if (withinDistance(1))
            basicAttack();
        if (target.player != null && target.player.getEquipment().getId(Equipment.SLOT_SHIELD) != 4156) {
            for (StatType statType : DRAIN) {
                target.player.getStats().get(statType).drain(4);
            }
            target.hit(new Hit(npc, null).randDamage(2, 5).ignoreDefence().ignorePrayer());
            target.player.sendMessage("<col=ff0000>The basilisk's piercing gaze drains your stats!");
            target.player.sendMessage("<col=ff0000>A mirror shield can protect you from this attack.");
        }
        return true;
    }
}
