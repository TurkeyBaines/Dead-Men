package io.dm.model.combat.special.melee;

import io.dm.cache.ItemDef;
import io.dm.model.combat.AttackStyle;
import io.dm.model.combat.AttackType;
import io.dm.model.combat.Hit;
import io.dm.model.combat.special.Special;
import io.dm.model.entity.Entity;
import io.dm.model.entity.player.Player;
import io.dm.model.stat.StatType;

public class Darklight implements Special {

    public static final StatType[] DRAIN_STATS = {StatType.Strength, StatType.Attack, StatType.Defence};

    @Override
    public boolean accept(ItemDef def, String name) {
        return def.id == 6746 || def.id == 19675;
    }

    @Override
    public boolean handle(Player player, Entity victim, AttackStyle attackStyle, AttackType attackType, int maxDamage) {
        boolean demon = victim.npc != null && victim.npc.getDef().demon;
        player.animate(2890);
        player.graphics(483);
        Hit hit = new Hit(player, attackStyle, attackType).randDamage(maxDamage).boostAttack(0.5);
        victim.hit(hit);
        if (!hit.isBlocked()) {
            for (StatType stat : DRAIN_STATS) {
                if(victim.player != null)
                    victim.player.getStats().get(stat).drain(0.05);
                else
                    victim.npc.getCombat().getStat(stat).drain(demon ? 0.1 : 0.05);
            }
        }
        return true;
    }

    @Override
    public int getDrainAmount() {
        return 50;
    }
}
