package io.dm.deadman.sigils.skilling;

import io.dm.deadman.sigils.Sigil;

public class Litheness extends Sigil {
    @Override
    public int ID() {
        return 23;
    }

    @Override
    public int Chance() {
        return 100;
    }

    @Override
    public int Cooldown() {
        return 10;
    }

    @Override
    public String name() {
        return "Litheness";
    }
}
