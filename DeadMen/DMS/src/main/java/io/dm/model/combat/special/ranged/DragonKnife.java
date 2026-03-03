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

public class DragonKnife implements Special {

    private static final Projectile PROJECTILE = new Projectile(699, 40, 36, 15, 37, 5, 15, 11);//699 = spec proj  8291 = anim

    @Override
    public boolean accept(ItemDef def, String name) {
        return def.id == 22804;
    }

    @Override
    public boolean handle(Player player, Entity victim, AttackStyle attackStyle, AttackType attackType, int maxDamage) {
        int delay = PROJECTILE.send(player, victim);
        player.animate(8291);
        Hit hit = new Hit(player, attackStyle, attackType).randDamage(maxDamage).clientDelay(delay);
        victim.hit(hit, new Hit(player, attackStyle, attackType).randDamage(maxDamage).clientDelay(delay));
        player.getCombat().removeAmmo(player.getEquipment().get(Equipment.SLOT_WEAPON), hit);
        return true;
    }

    @Override
    public int getDrainAmount() {
        return 25;
    }
}
