package io.dm.deadman.content.sigils.toggleable;

import io.dm.deadman.content.sigils.Sigil;

public class Slaughter extends Sigil {
    @Override
    public int ID() {
        return 3;
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
        return "Slaughter";
    }
}
