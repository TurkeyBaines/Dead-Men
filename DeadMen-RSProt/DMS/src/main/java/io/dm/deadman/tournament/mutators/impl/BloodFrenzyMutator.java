package io.dm.deadman.tournament.mutators.impl;

import io.dm.cache.Color;
import io.dm.deadman.tournament.mutators.Mutator;
import io.dm.model.entity.player.Player;

/**
 * BLOOD FRENZY MUTATOR
 * --------------------
 * Killing another player restores 10% of the killer's maximum HP.
 * Rewards aggressive play and makes consecutive kills self-sustaining —
 * the best fighters stay healthier, creating a risk/reward snowball effect.
 */
public class BloodFrenzyMutator extends Mutator {

    @Override
    public String name() {
        return "Blood Frenzy";
    }

    @Override
    public String[] description() {
        return new String[] {
                "Killing a player restores",
                "10% of your maximum HP.",
                "Stay aggressive to stay alive!"
        };
    }

    @Override
    public boolean hasAction() {
        return false;
    }

    @Override
    public void onPvpKill(Player killer, Player victim) {
        int restore = Math.max(1, killer.getMaxHp() / 10);
        killer.incrementHp(restore);
        killer.sendMessage(Color.RED.wrap("[Blood Frenzy] ")
                + "You drain vitality from your kill — +" + restore + " HP!");
    }
}
