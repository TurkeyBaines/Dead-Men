package io.dm.deadman.content.guard;

import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.npc.NPCCombat;
import io.dm.model.entity.player.Player;
import io.dm.model.skills.prayer.Prayer;

public class DMMGuardCombat extends NPCCombat {
    @Override
    public void init() {
    }

    @Override
    public void follow() {
        follow(1);
    }

    @Override
    public boolean attack() {

        if (target.player.dead() || !withinDistance(20)) {
            npc.remove();
            return false;
        }

        if (withinDistance(10)) {
            if (!target.isFrozen()) {
                npc.animate(2269);
                if (target.player.getPrayer().isActive(Prayer.PROTECT_FROM_MAGIC))
                    target.freeze(10, npc);
                else
                    target.freeze(20, npc);
                target.player.getPacketSender().sendMessage("You have been frozen by the Guard.", "", 0);
            }
            return true;
        } else if (withinDistance(1)) {
            basicAttack(info.attack_animation, info.attack_style, info.max_damage);
            return true;
        }

        if (!withinDistance(10)) {
            npc.getMovement().teleport(target.getPosition());
            npc.forceText("Don't you run away from me!");
        }
        return false;
    }
}
