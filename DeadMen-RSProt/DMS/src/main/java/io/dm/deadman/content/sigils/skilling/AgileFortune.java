package io.dm.deadman.content.sigils.skilling;

import io.dm.deadman.content.sigils.Sigil;

public class AgileFortune extends Sigil {
    @Override
    public int ID() {
        return 24;
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
        return "Agile Fortune";
    }
}
