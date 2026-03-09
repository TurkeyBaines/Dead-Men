package io.dm.deadman.mutators.impl;

import io.dm.deadman.mutators.Mutator;

public class EmptyMutator extends Mutator {
    @Override
    public String name() {
        return "None";
    }

    @Override
    public boolean hasAction() {
        return false;
    }
}
