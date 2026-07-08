package io.dm.deadman.tournament.mutators.impl;

import io.dm.deadman.tournament.mutators.Mutator;

/**
 * BOUNTY HUNTER MUTATOR
 * ---------------------
 * Every PvP kill pays out double blood money.
 * Ramps up the economy and makes the tournament feel high-stakes from the
 * first kill — players who snowball early get a big economic lead.
 */
public class BountyHunterMutator extends Mutator {

    @Override
    public String name() {
        return "Bounty Hunter";
    }

    @Override
    public String[] description() {
        return new String[] {
                "All PvP kills reward",
                "double Blood Money.",
                "Every death has a price!"
        };
    }

    @Override
    public boolean hasAction() {
        return false;
    }

    @Override
    public double pvpBloodMoneyMultiplier() {
        return 2.0;
    }
}
