package io.dm.deadman.content.sigils.utility;

import io.dm.deadman.content.sigils.Sigil;

public class PotionMaster extends Sigil {
    @Override
    public int ID() {
        return 32;
    }

    @Override
    public int Chance() {
        return 5;
    }

    @Override
    public int Cooldown() {
        return 10;
    }

    @Override
    public String name() {
        return "Potion Master";
    }

    public boolean effect() {
        if (isActionSuccessful())
            return true;

        return false;
    }
}
