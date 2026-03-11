package io.dm.deadman.mutators;

public abstract class Mutator {

    public abstract String name();
    public abstract String[] description();

    public abstract boolean hasAction();

    public void action() {}

    public void clear() {}
}
