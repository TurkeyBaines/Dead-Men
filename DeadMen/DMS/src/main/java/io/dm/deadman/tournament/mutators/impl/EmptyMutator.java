package io.dm.deadman.tournament.mutators.impl;

import io.dm.deadman.tournament.mutators.Mutator;

public class EmptyMutator extends Mutator {
    @Override
    public String name() {
        return "None";
    }

    @Override
    public String[] description() {
        return new String[]{"None"};
    }

    @Override
    public boolean hasAction() {
        return false;
    }
}
