package io.dm.deadman.mutators;

public abstract class Mutator {

    public abstract boolean hasAction();

    public void action() {}

    public void clear() {}
}
