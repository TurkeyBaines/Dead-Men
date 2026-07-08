package io.dm.panel.core;

/** A single (item id, amount) pair for the panel's inventory/bank views. */
public record ItemStack(int id, int amount) {}
