package io.dm.deadman.content.sigils.combat;

import io.dm.deadman.content.sigils.Sigil;

public class Titanium extends Sigil {
    @Override
    public int ID() {
        return 8;
    }

    @Override
    public int Chance() {
        return 0;
    }

    @Override
    public int Cooldown() {
        return 0;
    }

    @Override
    public String name() {
        return "Titanium";
    }

    @Override
    public boolean RestrictedWith() {
        return false; // Resistance
    }
}
