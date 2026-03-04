package io.dm.deadman.sigils.combat;

import io.dm.deadman.sigils.Sigil;
import io.dm.model.World;
import io.dm.model.combat.Hit;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.Player;

public class MenacingMage extends Sigil {
    @Override
    public int ID() {
        return 2;
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
        return "Menacing Mage";
    }

    @Override
    public boolean effect(Player player, Player target) {

        player.animate(812);

        World.startEvent(e -> {
            e.delay(3);
            target.hit(new Hit().fixedDamage(3));
            player.setHp(player.getHp() + 3);
            e.delay(1);
            target.hit(new Hit().fixedDamage(3));
            player.setHp(player.getHp() + 3);
            e.delay(1);
            target.hit(new Hit().fixedDamage(3));
            player.setHp(player.getHp() + 3);
            e.delay(1);
            target.hit(new Hit().fixedDamage(3));
            player.setHp(player.getHp() + 3);
            e.delay(1);
            target.hit(new Hit().fixedDamage(3));
            player.setHp(player.getHp() + 3);
            e.delay(1);
            target.hit(new Hit().fixedDamage(3));
            player.setHp(player.getHp() + 3);
        });
        return true;
    }

    @Override
    public boolean effect(Player player, NPC target) {
        player.animate(812);

        World.startEvent(e -> {
            e.delay(3);
            target.hit(new Hit().fixedDamage(3));
            player.setHp(player.getHp() + 3);
            e.delay(1);
            target.hit(new Hit().fixedDamage(3));
            player.setHp(player.getHp() + 3);
            e.delay(1);
            target.hit(new Hit().fixedDamage(3));
            player.setHp(player.getHp() + 3);
            e.delay(1);
            target.hit(new Hit().fixedDamage(3));
            player.setHp(player.getHp() + 3);
            e.delay(1);
            target.hit(new Hit().fixedDamage(3));
            player.setHp(player.getHp() + 3);
            e.delay(1);
            target.hit(new Hit().fixedDamage(3));
            player.setHp(player.getHp() + 3);
        });
        return true;
    }
}
