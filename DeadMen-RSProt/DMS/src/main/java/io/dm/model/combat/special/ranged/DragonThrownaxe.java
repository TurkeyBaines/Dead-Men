package io.dm.model.combat.special.ranged;

import io.dm.cache.ItemDef;
import io.dm.model.combat.AttackStyle;
import io.dm.model.combat.AttackType;
import io.dm.model.combat.Hit;
import io.dm.model.combat.special.Special;
import io.dm.model.entity.Entity;
import io.dm.model.entity.player.Player;
import io.dm.model.item.containers.Equipment;
import io.dm.model.map.Projectile;

public class DragonThrownaxe implements Special {

    private static final Projectile PROJECTILE = Projectile.thrown(1318, 11);

    @Override
    public boolean accept(ItemDef def, String name) {
        return def.id == 20849;
    }

    @Override
    public boolean handle(Player player, Entity victim, AttackStyle attackStyle, AttackType attackType, int maxDamage) {
        int delay = PROJECTILE.send(player, victim);
        player.animate(7521);
        player.graphics(1317, 120, 0);
        Hit hit = new Hit(player, attackStyle, attackType).randDamage(maxDamage).clientDelay(delay).boostAttack(0.25);
        victim.hit(hit);
        player.getCombat().removeAmmo(player.getEquipment().get(Equipment.SLOT_WEAPON), hit);
        return true;
    }

    @Override
    public int getDrainAmount() {
        return 25;
    }
}
