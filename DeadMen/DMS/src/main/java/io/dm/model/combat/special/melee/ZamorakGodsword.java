package io.dm.model.combat.special.melee;

import io.dm.cache.ItemDef;
import io.dm.model.combat.AttackStyle;
import io.dm.model.combat.AttackType;
import io.dm.model.combat.Hit;
import io.dm.model.combat.special.Special;
import io.dm.model.entity.Entity;
import io.dm.model.entity.player.Player;
import io.dm.model.item.containers.Equipment;

//Ice Cleave: Deal an attack that inflicts 10% more damage with double
//accuracy and freezes your target for 20 seconds if it successfully hits. (50%)
public class ZamorakGodsword implements Special {

    @Override
    public boolean accept(ItemDef def, String name) {
        return name.contains("zamorak godsword");
    }

    @Override
    public boolean handle(Player player, Entity target, AttackStyle attackStyle, AttackType attackType, int maxDamage) {
        player.animate(player.getEquipment().getId(Equipment.SLOT_WEAPON) == 11808 ? 7638 : 7639);
        player.graphics(1210);
        player.publicSound(3869);
        int damage = target.hit(new Hit(player, attackStyle, attackType).randDamage(maxDamage).boostDamage(0.10).boostAttack(1.0));
        if(damage > 0) {
            target.graphics(369);
            target.freeze(20, player);
        }
        return true;
    }

    @Override
    public int getDrainAmount() {
        return 50;
    }

}