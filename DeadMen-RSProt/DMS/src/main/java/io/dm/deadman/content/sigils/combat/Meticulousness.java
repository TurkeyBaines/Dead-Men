package io.dm.deadman.content.sigils.combat;

import io.dm.deadman.content.sigils.Sigil;

public class Meticulousness extends Sigil {
    //Upgrade from Deft Strikes

    @Override
    public int ID() {
        return 7;
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
        return "Meticulousness";
    }
}
