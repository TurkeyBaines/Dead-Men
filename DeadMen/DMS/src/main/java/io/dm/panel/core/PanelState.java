package io.dm.panel.core;

import io.dm.model.World;
import io.dm.model.entity.player.Player;

import java.util.Comparator;

/**
 * Shared panel state — chiefly "which online staff account the panel acts as."
 *
 * <p>Many existing server actions ({@code Punishment.*}, {@code CommandHandler.handle})
 * require an acting {@link Player} for messaging / logging context. The panel is a
 * console with no player of its own, so it borrows an online staff member as the actor.
 * We store the actor by <em>name</em> (never a cached {@code Player} reference, which
 * could go stale on logout) and re-resolve against {@link World} at use time.
 *
 * <p>If nothing is explicitly selected, {@link #resolveActingStaff()} auto-picks the
 * highest-ranked online staff member (lowest {@code PlayerGroup} ordinal), so the common
 * "owner is online" case just works with zero clicks.
 */
public final class PanelState {

    private static volatile String actingStaffName;

    private PanelState() {}

    public static void setActingStaff(Player player) {
        actingStaffName = player == null ? null : player.getName();
    }

    public static void setActingStaffName(String name) {
        actingStaffName = name;
    }

    public static String getActingStaffName() {
        return actingStaffName;
    }

    /**
     * The effective acting staff player, or {@code null} if no staff is online.
     * Prefers the explicit selection (if still online and still staff), otherwise the
     * highest-ranked online staff member.
     */
    public static Player resolveActingStaff() {
        String name = actingStaffName;
        if (name != null) {
            Player explicit = World.getPlayer(name);
            if (explicit != null && explicit.isStaff()) {
                return explicit;
            }
        }
        try {
            return World.getPlayerStream()
                    .filter(p -> p != null && p.isStaff())
                    .min(Comparator.comparingInt(p -> p.getPrimaryGroup() == null
                            ? Integer.MAX_VALUE : p.getPrimaryGroup().ordinal()))
                    .orElse(null);
        } catch (Throwable t) {
            return null;
        }
    }
}
