package io.dm.deadman.content.sigils.combat;

import io.dm.deadman.content.sigils.Sigil;

public class Resistance extends Sigil {
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
        return "Resistance";
    }

    @Override
    public boolean RestrictedWith() {
        return false; // Titanium
    }
}
