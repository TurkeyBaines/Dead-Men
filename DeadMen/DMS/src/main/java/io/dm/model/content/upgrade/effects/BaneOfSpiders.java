package io.dm.model.content.upgrade.effects;

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
public class BaneOfSpiders extends ItemUpgrade {

    @Override
    public void preTargetDefend(Player player, Entity target, Item item, Hit hit) {
        if (hit.attacker != null && hit.attacker.npc != null && hit.attacker.npc.getDef().name.toLowerCase().contains("spider") || target.npc.getDef().name.equalsIgnoreCase("sarachnis")) {
            hit.boostDamage(0.1);
        }
    }

}
