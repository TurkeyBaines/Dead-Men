package io.dm.deadman.content.guard;

import io.dm.cache.Color;
import io.dm.model.entity.player.Player;

/**
 * Skull system.
 * <p>
 * A player is skulled while {@code Player.skullTicks > 0}. The timer lives on the player (so it is saved with the
 * character and survives logging out, instead of living in a static map that leaked players and lost the skull on
 * relog), counts down one per game tick from {@link #process(Player)}, and the skull icon follows it.
 * <p>
 * Skull rules, all in {@link #attacked(Player, Player)} so every damage source behaves the same:
 * <ul>
 *     <li>attacking a player skulls you, for longer if you out-level them;</li>
 *     <li>no skull if the victim attacked you first (you are retaliating), or is already skulled;</li>
 *     <li>attacking again while skulled refreshes the timer to the new duration, it never stacks;</li>
 *     <li>dying clears the skull.</li>
 * </ul>
 * Skulled players are attacked by the safe zone guards ({@link DMMGuard}).
 */
public class Skull {

    /** Durations in game ticks (0.6s). */
    public static final int SHORT_SKULL = 500;
    public static final int MED_SKULL = 750;
    public static final int LONG_SKULL = 1000;

    /** Combat level lead needed over the victim for the medium / long skull. */
    private static final int MED_LEVEL_LEAD = 15;
    private static final int LONG_LEVEL_LEAD = 30;

    /** A victim that attacked the player within this many ticks counts as having started the fight. */
    private static final int RETALIATION_TICKS = 100;

    public static boolean hasSkull(Player player) {
        return player.skullTicks > 0;
    }

    public static int remainingTicks(Player player) {
        return Math.max(0, player.skullTicks);
    }

    /**
     * Skulls the player for {@code ticks}. An existing longer skull is kept, a shorter one is extended to {@code ticks}.
     */
    public static void skull(Player player, int ticks) {
        if (ticks <= 0)
            return;
        boolean newSkull = player.skullTicks <= 0;
        player.skullTicks = Math.max(player.skullTicks, ticks);
        if (newSkull) {
            applyIcon(player);
            player.sendMessage(Color.RED.wrap("A skull appears above your head. The guards of safe zones will attack you on sight!"));
        }
    }

    /**
     * Removes the skull silently.
     */
    public static void clear(Player player) {
        player.skullTicks = 0;
        applyIcon(player);
    }

    /**
     * Death removes the skull.
     */
    public static void onDeath(Player player) {
        clear(player);
    }

    /**
     * Counts the timer down. Call once per game tick per player.
     */
    public static void process(Player player) {
        if (player.skullTicks > 0 && --player.skullTicks == 0) {
            applyIcon(player);
            player.sendMessage("Your skull fades away.");
        }
    }

    /**
     * Puts the head icon in line with the timer (login, skull, clear). Only touches the appearance when it differs.
     */
    public static void applyIcon(Player player) {
        int icon = hasSkull(player) ? 0 : -1;
        if (player.getAppearance().getSkullIcon() != icon)
            player.getAppearance().setSkullIcon(icon);
    }

    /**
     * The one place that decides whether attacking a player skulls the attacker.
     */
    public static void attacked(Player attacker, Player victim) {
        if (attacker == null || victim == null || attacker == victim)
            return;
        if (hasSkull(victim))
            return; // fair game, already marked
        if (victim.getCombat().lastPlayerAttacked == attacker.getUserId() && victim.getCombat().isAttacking(RETALIATION_TICKS))
            return; // they started it
        skull(attacker, duration(attacker.getCombat().getLevel() - victim.getCombat().getLevel()));
    }

    private static int duration(int levelLead) {
        if (levelLead > LONG_LEVEL_LEAD)
            return LONG_SKULL;
        if (levelLead > MED_LEVEL_LEAD)
            return MED_SKULL;
        return SHORT_SKULL;
    }

}
