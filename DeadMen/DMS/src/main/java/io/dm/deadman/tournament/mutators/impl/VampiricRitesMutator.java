package io.dm.deadman.tournament.mutators.impl;

import io.dm.deadman.tournament.mutators.Mutator;

public class VampiricRitesMutator extends Mutator {
    @Override
    public String name() {
        return "Vampiric Rites";
    }

    @Override
    public String[] description() {
        return new String[] {
                "Natural HP Regeneration is",
                "disabled , food and potions",
                "will continue to work as normal"
        };
    }

    @Override
    public boolean hasAction() {
        return false;
    }
}
