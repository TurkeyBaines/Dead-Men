package io.dm.deadman.content.sigils.utility;

import io.dm.deadman.content.sigils.Sigil;

public class RevokedLimitation extends Sigil {
    @Override
    public int ID() {
        return 34;
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
        return "Revoked Limitation";
    }
}
