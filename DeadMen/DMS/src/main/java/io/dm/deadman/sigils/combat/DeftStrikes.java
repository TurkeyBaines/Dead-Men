package io.dm.deadman.sigils.combat;

import io.dm.deadman.sigils.Sigil;

public class DeftStrikes extends Sigil {
    @Override
    public int ID() {
        return 4;
    }

    @Override
    public int Chance() {
        return 100;
    }

    @Override
    public int Cooldown() {
        return 0;
    }

    @Override
    public String name() {
        return "Deft Strikes";
    }
}
