package io.dm.model.combat.special.melee;

import com.google.common.collect.Lists;
import io.dm.cache.ItemDef;
import io.dm.model.combat.AttackStyle;
import io.dm.model.combat.AttackType;
import io.dm.model.combat.Hit;
import io.dm.model.combat.special.Special;
import io.dm.model.entity.Entity;
import io.dm.model.entity.player.Player;
import io.dm.utility.Misc;

import java.util.List;

/**
 * @author ReverendDread on 8/6/2020
 * https://www.rune-server.ee/members/reverenddread/
 * @project Kronos
 */
public class Dragon2hSword implements Special {

    @Override
    public boolean accept(ItemDef def, String name) {
        return name.contains("dragon 2h sword");
    }

    @Override
    public boolean handle(Player player, Entity victim, AttackStyle attackStyle, AttackType attackType, int maxDamage) {
        player.animate(3157);
        player.graphics(559);
        List<Entity> targets = Lists.newArrayList();
        if (player.inMulti()) {
            player.forLocalEntity(e -> {
                if (targets.size() < 14) {
                    if(Misc.getDistance(player.getPosition(), e.getPosition()) > 1)
                        return;
                    if(!player.getCombat().canAttack(e, false))
                        return;
                    targets.add(e);
                }
            });
            targets.forEach(e -> e.hit(new Hit(player, attackStyle, attackType).randDamage(maxDamage)));
        } else {
            //Single attack
            victim.hit(new Hit(player, attackStyle, attackType).randDamage(maxDamage));
        }
        return true;
    }

    @Override
    public int getDrainAmount() {
        return 60;
    }
}
