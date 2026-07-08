package io.dm.deadman.content.sigils.skilling;

import io.dm.deadman.content.sigils.Sigil;

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
