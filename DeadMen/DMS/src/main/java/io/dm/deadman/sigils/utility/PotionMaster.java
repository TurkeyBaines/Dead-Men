package io.dm.deadman.sigils.utility;

import io.dm.deadman.sigils.Sigil;
import io.dm.model.entity.player.Player;

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
