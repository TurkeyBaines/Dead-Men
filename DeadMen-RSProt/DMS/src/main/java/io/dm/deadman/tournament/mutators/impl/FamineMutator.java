package io.dm.deadman.tournament.mutators.impl;

import io.dm.deadman.tournament.mutators.Mutator;

/**
 * FAMINE MUTATOR
 * --------------
 * Food heals half as much as normal.  Natural HP regen is unaffected;
 * potions are unaffected.  Forces players to stretch supplies and makes
 * every fight cost more than usual.
 */
public class FamineMutator extends Mutator {

    @Override
    public String name() {
        return "Famine";
    }

    @Override
    public String[] description() {
        return new String[] {
                "All food heals for",
                "50% of its normal value.",
                "Potions are unaffected."
        };
    }

    @Override
    public boolean hasAction() {
        return false;
    }

    @Override
    public int modifyFoodHeal(int base) {
        return Math.max(1, base / 2);
    }
}
