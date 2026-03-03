package io.dm.model.activities.inferno.monsters;

import io.dm.api.utils.Random;
import io.dm.model.activities.inferno.Inferno;
import io.dm.model.combat.AttackStyle;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.npc.NPCCombat;
import io.dm.model.map.Projectile;
import io.dm.model.map.dynamic.DynamicMap;
import io.dm.utility.TickDelay;

import java.util.stream.Collectors;

public class JalZek extends NPCCombat { // Mage

    private static final Projectile MAGIC_PROJECTILE = new Projectile(1376, 96, 31, 28, 36, 8, 16, 192).regionBased();

    private TickDelay reviveCooldown = new TickDelay();

    @Override
    public void init() {
        reviveCooldown.delay(Random.get(30, 50));
    }

    @Override
    public void follow() {
        follow(16);
    }

    @Override
    public boolean attack() {
        if(withinDistance(1)) {
            if(Random.rollDie(3)) {
                basicAttack(info.attack_animation, info.attack_style, info.max_damage);
                return true;
            }
        } else if(!withinDistance(16)) {
            /**
             * Not in ranged distance
             */
            return false;
        }
        if (npc.getId() != 7703 && !reviveCooldown.isDelayed()) {
            revive();
            return true;
        }
        projectileAttack(MAGIC_PROJECTILE, 7610, AttackStyle.MAGIC, info.max_damage);
        return true;
    }

    private void revive() {
        if (possibleReviveTargetsCount() <= 0)
            return;
        DynamicMap map = Inferno.getInstance(npc).getMap();
        if (map == null)
            return;
        reviveCooldown.delay(Random.get(60, 120));
        delayAttack(15);
        npc.addEvent(event -> {
            npc.animate(7611);
            event.delay(6);
            if (possibleReviveTargetsCount() <= 0)
                return;
            NPC corpse = Random.get(map.getNpcs().stream().filter(n -> n.getCombat().isDead() && n.get("ZEK_REVIVE") == null).collect(Collectors.toList()));
            corpse.getPosition().set(map.convertX(2270), map.convertY(5344));
            corpse.getCombat().restore();
            corpse.setHp(corpse.getHp() / 2);
            corpse.getCombat().setDead(false);
            corpse.setHidden(false);
            corpse.set("ZEK_REVIVE", Boolean.TRUE);
            corpse.unlock();
            event.delay(10);
            corpse.attackTargetPlayer();
        });
    }

    private int possibleReviveTargetsCount() {
        DynamicMap map = Inferno.getInstance(npc).getMap();
        if (map == null)
            return 0;
        return (int) map.getNpcs().stream().filter(n -> n.getCombat().isDead() && n.get("ZEK_REVIVE") == null).count();
    }

}