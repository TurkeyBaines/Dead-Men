package io.dm.model.content.upgrade.effects;

import io.dm.api.utils.Random;
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
public class GuthixsBalance extends ItemUpgrade {
    @Override
    public void preTargetDefend(Player player, Entity target, Item item, Hit hit) {
        if (Random.rollDie(10)) {
            target.graphics(436, 48, 0);
            player.graphics(436, 48, 0);
            player.privateSound(958);
            player.incrementHp(10);
            target.incrementHp(10);
            player.sendMessage("Guthix's Balance has healed you and your opponent!");
        }
    }
}