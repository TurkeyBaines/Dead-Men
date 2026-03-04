package io.dm.deadman.sigils.combat;

import io.dm.deadman.sigils.Sigil;
import io.dm.model.entity.player.Player;

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
