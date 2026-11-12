package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.entity.player.Player;
import io.dm.model.item.actions.ItemAction;

public class SigilOfTheWellFed extends Sigil {

    @Override
    public String name() {
        return "Sigil of the Well Fed";
    }

    @Override
    public String shortName() {
        return "Well Fed";
    }

    @Override
    protected int chance() {
        return 5;
    }

    @Override
    public int unattuned() {
        return 28499;
    }

    @Override
    public int attuned() {
        return 28498;
    }

    @Override
    public boolean effect(Player p, Object... params) {
        int heal = (int) params[0];
        int current = p.getHp();
        int max = p.getMaxHp();

        if (current + heal > max) heal = max - current;
        if (heal <= 0) return false;

        p.setHp(current + heal);
        p.sendFilteredMessage("Your Sigil of the Well Fed heals you for an extra " + heal + " Hitpoints.");

        return true;
    }

    public SigilOfTheWellFed() {
        ItemAction.registerInventory(unattuned(), "Attune", attune());
        ItemAction.registerInventory(attuned(), "Unattune", unattune());
    }
}
