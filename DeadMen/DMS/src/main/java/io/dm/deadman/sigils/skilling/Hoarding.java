package io.dm.deadman.sigils.skilling;

import io.dm.deadman.sigils.Sigil;

public class Hoarding extends Sigil {
    @Override
    public int ID() {
        return 22;
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
        return "Hoarding";
    }
}
