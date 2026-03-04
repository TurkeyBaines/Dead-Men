package io.dm.deadman.sigils.combat;

import io.dm.deadman.sigils.Sigil;
import io.dm.model.World;
import io.dm.model.combat.Hit;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.Player;

public class FormidableFighter extends Sigil {
    @Override
    public int ID() {
        return 0;
    }

    @Override
    public int Chance() {
        return 30;
    }

    @Override
    public int Cooldown() {
        return 17;
    }

    @Override
    public String name() {
        return "Formidable Fighter";
    }

    @Override
    public boolean effect(Player player, Player target) {
        player.animate(812);

        World.startEvent(e -> {
            e.delay(player.getEquipment().get(3).getDef().weaponType.attackTicks);
            target.hit(new Hit().fixedDamage(10));
        });
        return true;
    }

    @Override
    public boolean effect(Player player, NPC target) {
        player.animate(812);

        World.startEvent(e -> {
            e.delay(player.getEquipment().get(3).getDef().weaponType.attackTicks);
            target.hit(new Hit().fixedDamage(10));
        });
        return true;
    }
}
