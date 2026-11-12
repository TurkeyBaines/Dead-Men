package io.dm.deadman.content.sigils;

import io.dm.api.utils.Random;
import io.dm.model.entity.player.Player;
import io.dm.model.item.actions.ItemAction;

public abstract class Sigil {

    public void process() {

    }

    public boolean proc() {
        int rng = Random.get(100);
        return rng < chance();
    }

    public ItemAction attune() {
        return (p, i) -> {
            if (!p.getInventory().contains(unattuned())) return;

            if (p.canAttune()) p.attune(unattuned());
        };
    }

    public ItemAction unattune() {
        return (p, i) -> {
            if (!p.getInventory().contains(attuned())) return;

            if (p.hasSigil(Sigils.Tier1.SIGIL_OF_AGILE_FORTUNE)) p.unattune(attuned());
        };
    }

    public abstract String name();
    public abstract String shortName();
    protected abstract int chance();
    public abstract int unattuned();
    public abstract int attuned();

    public boolean effect() {return false;}
    public boolean effect(Player player) {return false;}
    public boolean effect(Player player, Object... params) {return false;}
}
