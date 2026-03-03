package io.dm.model.content.upgrade.effects;

import io.dm.api.utils.Random;
import io.dm.model.World;
import io.dm.model.combat.Hit;
import io.dm.model.content.upgrade.ItemUpgrade;
import io.dm.model.entity.Entity;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;

/*
 * @project Kronos
 * @author Patrity - https://github.com/Patrity
 * Created on - 8/10/2020
 */
public class ZamoraksCurse extends ItemUpgrade {
    @Override
    public void preTargetDefend(Player player, Entity target, Item item, Hit hit) {
        if (target.getHp() - hit.damage <= 0) {
            if (Random.rollDie(10)) {
                World.startEvent(e -> {
                    e.delay(1);
                    target.graphics(437, 0, 0);
                    player.hit(new Hit().fixedDamage(10));
                    player.sendMessage("Zamorak's Curse inflicts damage!");
                });
            }
        }
    }
}
