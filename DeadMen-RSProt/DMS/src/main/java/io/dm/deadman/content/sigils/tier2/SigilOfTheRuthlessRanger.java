package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.World;
import io.dm.model.combat.Hit;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.Player;
import io.dm.model.item.actions.ItemAction;

public class SigilOfTheRuthlessRanger extends Sigil {

    @Override
    public String name() {
        return "Sigil of the Ruthless Ranger";
    }

    @Override
    public String shortName() {
        return "Ruthless Ranger";
    }

    @Override
    protected int chance() {
        return 15;
    }

    @Override
    public int unattuned() {
        return 26072;
    }

    @Override
    public int attuned() {
        return 26071;
    }

    @Override
    public boolean effect(Player p, Object... params) {
        if (params[0] instanceof Player) {
            Player target = (Player) params[0];
            p.animate(812);
            World.startEvent(e -> {
                target.hit(new Hit().fixedDamage(runDamage(target)));
                target.getMovement().drainEnergy(2);
                e.delay(1);
                target.hit(new Hit().fixedDamage(runDamage(target)));
                target.getMovement().drainEnergy(3);
                e.delay(1);
                target.hit(new Hit().fixedDamage(runDamage(target)));
                target.getMovement().drainEnergy(2);
            });
        } else if (params[0] instanceof NPC) {
            NPC target = (NPC) params[0];
            p.animate(812);
            World.startEvent(e -> {
                target.hit(new Hit().fixedDamage(2));
                e.delay(1);
                target.hit(new Hit().fixedDamage(3));
                e.delay(1);
                target.hit(new Hit().fixedDamage(2));
            });
        } else {
            return false;
        }
        return true;
    }

    private int runDamage(Player target) {
        return (target.energyUnits < 1000 ? 30 : 15) / 6;
    }

    public SigilOfTheRuthlessRanger() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
