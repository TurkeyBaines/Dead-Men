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
public class Protector extends ItemUpgrade {

    @Override
    public void preDefend(Player player, Entity target, Item item, Hit hit) {
        if (Random.rollDie(20)  && hit.damage != 0) {
            hit.damage *= 0.8;
        }
    }
}
