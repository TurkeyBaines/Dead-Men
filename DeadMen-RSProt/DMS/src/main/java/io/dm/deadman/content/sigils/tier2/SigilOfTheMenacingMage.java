package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.World;
import io.dm.model.combat.Hit;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.item.actions.ItemAction;

public class SigilOfTheMenacingMage extends Sigil {


    @Override
    public String name() {
        return "Sigil of the Menacing Mage";
    }

    @Override
    public String shortName() {
        return "Menacing Mage";
    }

    @Override
    protected int chance() {
        return 15;
    }

    @Override
    public int unattuned() {
        return 26078;
    }

    @Override
    public int attuned() {
        return 26077;
    }

    @Override
    public boolean effect(Player p, Object... params) {
        if (params[0] instanceof Player) {
            Player target = (Player) params[0];
            World.startEvent(e -> {
                p.animate(10655);
                p.graphics(1977);
                e.delay(3);
                target.hit(new Hit().fixedDamage(3));
                p.setHp(p.getHp() + 3);
                e.delay(1);
                target.hit(new Hit().fixedDamage(3));
                p.setHp(p.getHp() + 3);
                e.delay(1);
                target.hit(new Hit().fixedDamage(3));
                p.setHp(p.getHp() + 3);
                e.delay(1);
                target.hit(new Hit().fixedDamage(3));
                p.setHp(p.getHp() + 3);
                e.delay(1);
                target.hit(new Hit().fixedDamage(3));
                p.setHp(p.getHp() + 3);
                e.delay(1);
                target.hit(new Hit().fixedDamage(3));
                p.setHp(p.getHp() + 3);
            });
        } else if (params[0] instanceof NPC) {
            NPC target = (NPC) params[0];
            World.startEvent(e -> {
                p.animate(10655);
                p.graphics(1977);
                e.delay(3);
                target.hit(new Hit().fixedDamage(3));
                p.setHp(p.getHp() + 3);
                e.delay(1);
                target.hit(new Hit().fixedDamage(3));
                p.setHp(p.getHp() + 3);
                e.delay(1);
                target.hit(new Hit().fixedDamage(3));
                p.setHp(p.getHp() + 3);
                e.delay(1);
                target.hit(new Hit().fixedDamage(3));
                p.setHp(p.getHp() + 3);
                e.delay(1);
                target.hit(new Hit().fixedDamage(3));
                p.setHp(p.getHp() + 3);
                e.delay(1);
                target.hit(new Hit().fixedDamage(3));
                p.setHp(p.getHp() + 3);
            });
        } else {
            return false;
        }
        return true;
    }

    public SigilOfTheMenacingMage() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
