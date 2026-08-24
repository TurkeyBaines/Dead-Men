package io.dm.deadman.tournament.mutators;

import io.dm.model.entity.player.Player;

public abstract class Mutator {

    public abstract String name();
    public abstract String[] description();

    public abstract boolean hasAction();

    public void action() {}
    public void clear() {}

    /** Return true to suppress natural HP regeneration (food/potions still work). */
    public boolean suppressHpRegen() { return false; }

    /**
     * Modify how much HP a food item heals.
     * Return value replaces the base heal amount — clamp to ≥1 inside the impl.
     */
    public int modifyFoodHeal(int base) { return base; }

    /** Multiplier applied to PvP blood money reward (e.g. 2 = double). */
    public double pvpBloodMoneyMultiplier() { return 1.0; }

    /** Called immediately after a player is killed in PvP. */
    public void onPvpKill(Player killer, Player victim) {}
}
