package io.dm.model.content.upgrade.effects;

import io.dm.api.utils.Random;
import io.dm.model.combat.Hit;
import io.dm.model.content.upgrade.ItemUpgrade;
import io.dm.model.entity.Entity;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;

/**
 * @author ReverendDread on 6/18/2020
 * https://www.rune-server.ee/members/reverenddread/
 * @project Kronos
 */
public class Recoil extends ItemUpgrade {

    @Override
    public void postPlayerDamage(Player player, Entity target, Item item, Hit hit) {
        if (hit.attacker != null && hit.attacker.npc != null && hit.damage > 0 && Random.rollDie(10)) {
            hit.attacker.npc.hit(new Hit().fixedDamage(hit.damage / 2));
        }
    }

}
