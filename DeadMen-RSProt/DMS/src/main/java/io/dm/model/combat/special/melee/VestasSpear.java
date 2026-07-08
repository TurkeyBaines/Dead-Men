package io.dm.model.combat.special.melee;

import io.dm.cache.ItemDef;
import io.dm.model.combat.AttackStyle;
import io.dm.model.combat.AttackType;
import io.dm.model.combat.Hit;
import io.dm.model.combat.special.Special;
import io.dm.model.entity.Entity;
import io.dm.model.entity.player.Player;
import io.dm.utility.Misc;

import java.util.LinkedList;

public class VestasSpear implements Special {
    @Override
    public boolean accept(ItemDef def, String name) {
        return name.equalsIgnoreCase("vesta's spear");
    }

    @Override
    public boolean handle(Player player, Entity victim, AttackStyle attackStyle, AttackType attackType, int maxDamage) {
        player.animate(8184);
        player.graphics(1627);
        player.vestasSpearSpecial.delay(8);
        int targetsHit = 1;
        victim.hit(new Hit(player, attackStyle, attackType).randDamage(maxDamage / 2));
        if (player.inMulti()) {
            LinkedList<Entity> targets = new LinkedList<>();
            player.localPlayers().forEach(p -> {
                if (p != player)
                    targets.add(p);
            });
            targets.addAll(player.localNpcs());
            targets.remove(victim);
            for (Entity e : targets) {
                if (player.getCombat().canAttack(e, false) && Misc.getEffectiveDistance(player, e) <= 8) {
                    targetsHit++;
                    e.hit(new Hit(player, attackStyle, attackType).randDamage(maxDamage / 2));
                    if (targetsHit >= 16)
                        break;
                }
            }
        }
        return true;
    }

    @Override
    public int getDrainAmount() {
        return 50;
    }
}
