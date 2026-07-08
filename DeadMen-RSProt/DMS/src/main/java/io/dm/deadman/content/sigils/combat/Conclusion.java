package io.dm.deadman.content.sigils.combat;

import io.dm.deadman.content.sigils.Sigil;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.utils.Config;

public class Conclusion extends Sigil {
    @Override
    public int ID() {
        return 9;
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
        return "Conclusion";
    }

    @Override
    public boolean effect(Player player) {
        //TODO add 'ticks since last pvp attack'
        //TODO add 'radius of 15 tiles'
        int curr = Config.SPECIAL_ENERGY.get(player);
        int toAdd = 100 - curr;
        if (toAdd > 10) toAdd = 10;

        Config.SPECIAL_ENERGY.set(player, toAdd);

        return true;
    }
}
