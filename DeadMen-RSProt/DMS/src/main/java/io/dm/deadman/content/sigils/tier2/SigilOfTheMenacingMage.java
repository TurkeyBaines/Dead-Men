package io.dm.deadman.content.sigils.tier2;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
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

    static {
        final int unattuned = 26078, attuned = 26077;

        ItemAction.registerInventory(unattuned, "Attune", ((p, i) -> {
            if (!p.getInventory().contains(unattuned))
                return;

            if (p.canAttune()) {
                if (p.attune(unattuned)) {
                    int slot = i.getSlot();
                    p.getInventory().remove(i);
                    Item item = new Item(attuned, 1);
                    item.setSlot(slot);
                    p.getInventory().add(item);
                }
            }
        }));

        ItemAction.registerInventory(attuned, "Unattune", ((p, i) -> {
            if (!p.getInventory().contains(attuned)) {
                return;
            }

            if (p.hasSigil(Sigils.Tier2.SIGIL_OF_THE_MENACING_MAGE)) {
                boolean success = p.unattune(unattuned);
                if (success) {
                    int slot = i.getSlot();
                    p.getInventory().remove(i);
                    Item item = new Item(unattuned, 1);
                    item.setSlot(slot);
                    p.getInventory().add(item);
                }
            }
        }));
    }
}
