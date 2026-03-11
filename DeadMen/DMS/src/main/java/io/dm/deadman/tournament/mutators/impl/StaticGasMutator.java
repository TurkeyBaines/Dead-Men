package io.dm.deadman.tournament.mutators.impl;

import io.dm.cache.Color;
import io.dm.deadman.tournament.gas.GasArea;
import io.dm.deadman.tournament.mutators.Mutator;
import io.dm.model.World;
import io.dm.model.map.Bounds;

public class StaticGasMutator extends Mutator {

    private Bounds bounds;
    private GasArea gasArea;

    @Override
    public String name() {
        return "Static Gas";
    }

    @Override
    public String[] description() {
        return new String[] {
                "Gas will prevent access to",
                "areas of the game for the",
                "duration of the tournament"
        };
    }

    @Override
    public boolean hasAction() {
        return true;
    }

    @Override
    public void action() {
        //Select 2 random bounds
        bounds = StaticGasRegion.MISTHALIN.bounds;
        if (gasArea != null) gasArea = null;

        gasArea = new GasArea(bounds);
        gasArea.create();
        World.players.forEach(p -> p.sendMessage(Color.GOLD.wrap("[Server] ") + Color.DARK_RED.wrap("The Static Gas Mutator has restricted access to the region of Misthalin. Attempting to enter will result in constant damage!")));
    }

    @Override
    public void clear() {
        gasArea.stop = true;
        gasArea.remove();
        gasArea = null;
    }
}
