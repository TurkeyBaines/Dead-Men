package io.dm.deadman.sigils.utility;

import io.dm.deadman.sigils.Sigil;

public class EternalBelief extends Sigil {
    @Override
    public int ID() {
        return 35;
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
        return "Eternal Belief";
    }
}
