package io.dm.model.combat.special.ranged;

import io.dm.cache.ItemDef;
import io.dm.model.combat.AttackStyle;
import io.dm.model.combat.AttackType;
import io.dm.model.combat.Hit;
import io.dm.model.combat.special.Special;
import io.dm.model.entity.Entity;
import io.dm.model.entity.player.Player;
import io.dm.model.map.Projectile;

public class MorrigansJavelin implements Special {

    private static final Projectile PROJECTILE = Projectile.thrown(1622, 11);

    @Override
    public boolean accept(ItemDef def, String name) {
        return name.equalsIgnoreCase("morrigan's javelin");
    }

    @Override
    public boolean handle(Player player, Entity victim, AttackStyle attackStyle, AttackType attackType, int maxDamage) {
        player.animate(806);
        player.graphics(1621, 96, 0);
        int delay = PROJECTILE.send(player, victim);
        int damage = victim.hit(new Hit(player, attackStyle, attackType).randDamage(maxDamage).clientDelay(delay));
        if (damage > 0) {
            victim.addEvent(event -> {
                int bleed = damage;
                event.delay(3);
                while (bleed > 0 && !victim.getCombat().isDead()) {
                    int damageToDeal = Math.min(5, bleed);
                    bleed -= damageToDeal;
                    victim.hit(new Hit().fixedDamage(damageToDeal).ignorePrayer().ignoreDefence());
                    event.delay(3);
                }
            });
        }
        return true;
    }

    @Override
    public int getDrainAmount() {
        return 50;
    }
}
