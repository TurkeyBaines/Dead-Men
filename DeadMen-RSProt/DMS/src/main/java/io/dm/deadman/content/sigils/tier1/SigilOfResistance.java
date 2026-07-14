package io.dm.deadman.content.sigils.tier1;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.model.entity.player.Player;

public class SigilOfResistance extends Sigil {
    @Override
    public String name() {
        return Sigils.Tier1.SIGIL_OF_RESISTANCE.name();
    }

    @Override
    protected int chance() {
        return 0;
    }

    @Override
    public int unattuned() {
        return 0;
    }

    @Override
    public int attuned() {
        return 0;
    }

    @Override
    public boolean effect(Player p, Object... params) {



        return true;
    }
}
