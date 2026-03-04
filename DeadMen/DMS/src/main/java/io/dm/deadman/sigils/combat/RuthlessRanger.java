package io.dm.deadman.sigils.combat;

import io.dm.deadman.sigils.Sigil;
import io.dm.model.World;
import io.dm.model.combat.Hit;
import io.dm.model.combat.HitType;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.Player;

public class RuthlessRanger extends Sigil {

    @Override
    public int ID() {
        return 1;
    }

    @Override
    public int Chance() {
        return 15;
    }

    @Override
    public int Cooldown() {
        return 0;
    }

    @Override
    public String name() {
        return "Ruthless Ranger";
    }

    @Override
    public boolean effect(Player player, Player target) {
        player.animate(812);


        World.startEvent(e -> {
            target.hit(new Hit().fixedDamage(checkRun(target) / 6));
            target.getMovement().drainEnergy(2);
            e.delay(1);
            target.hit(new Hit().fixedDamage(checkRun(target) / 6));
            target.getMovement().drainEnergy(3);
            e.delay(1);
            target.hit(new Hit().fixedDamage(checkRun(target) / 6));
            target.getMovement().drainEnergy(2);
            e.delay(1);
            target.hit(new Hit().fixedDamage(checkRun(target) / 6));
            target.getMovement().drainEnergy(3);
            e.delay(1);
            target.hit(new Hit().fixedDamage(checkRun(target) / 6));
            target.getMovement().drainEnergy(2);
            e.delay(1);
            target.hit(new Hit().fixedDamage(checkRun(target) / 6));
            target.getMovement().drainEnergy(3);
        });
        return true;
    }

    private int checkRun(Player target) {
        if (target.energyUnits < 1000)
            return 30;
        return 15;
    }

    @Override
    public boolean effect(Player player, NPC target) {
        player.animate(812);

        World.startEvent(e -> {
            target.hit(new Hit().fixedDamage(2));
            e.delay(1);
            target.hit(new Hit().fixedDamage(3));
            e.delay(1);
            target.hit(new Hit().fixedDamage(2));
            e.delay(1);
            target.hit(new Hit().fixedDamage(3));
            e.delay(1);
            target.hit(new Hit().fixedDamage(2));
            e.delay(1);
            target.hit(new Hit().fixedDamage(3));
        });
        return true;
    }

}
