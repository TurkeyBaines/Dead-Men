package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils.*;
import io.dm.model.World;
import io.dm.model.combat.Hit;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.Player;
import io.dm.model.item.actions.ItemAction;

public class SigilOfTheFormidableFighter extends Sigil {

    @Override
    public String name() {
        return "Sigil of the Formidable Fighter";
    }

    @Override
    public String shortName() {
        return "Formidable Fighter";
    }

    @Override
    protected int chance() {
        return 30;
    }

    @Override
    public int unattuned() {
        return 25997;
    }

    @Override
    public int attuned() {
        return 25996;
    }

    @Override
    public boolean effect(Player p, Object... params) {
        if (params[0] instanceof Player) {
            Player target = (Player) params[0];
            p.animate(812);
            World.startEvent(e -> {
                e.delay(2);
                target.hit(new Hit().fixedDamage(10));
            });
        } else if (params[0] instanceof NPC) {
            NPC target = (NPC) params[0];
            p.animate(812);
            World.startEvent(e -> {
                e.delay(2);
                target.hit(new Hit().fixedDamage(10));
            });
        } else {
            return false;
        }
        return true;
    }

    public SigilOfTheFormidableFighter() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
