package io.dm.deadman.sigils.skilling;

import io.dm.deadman.sigils.Sigil;

public class Deception extends Sigil {

    // Automatically re-pickpocket an NPC until you can no longer do so.
    // Drastically improves pickpocketing success rates at early levels.

    @Override
    public int ID() {
        return 21;
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
        return "Deception";
    }
}
