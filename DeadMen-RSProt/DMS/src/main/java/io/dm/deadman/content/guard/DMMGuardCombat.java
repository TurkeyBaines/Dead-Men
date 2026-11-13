package io.dm.deadman.content.guard;

import java.util.concurrent.ThreadLocalRandom;
import io.dm.deadman.content.areas.SafeZone;
import io.dm.model.combat.AttackStyle;
import io.dm.model.combat.Hit;
import io.dm.model.entity.Entity;
import io.dm.model.entity.npc.NPCCombat;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.Widget;
import io.dm.model.map.Position;
import io.dm.model.map.Projectile;
import io.dm.model.map.route.routes.DumbRoute;
import io.dm.model.skills.prayer.Prayer;
import io.dm.utility.Misc;

import java.util.Comparator;

/**
 * Safe zone guard combat.
 * <ul>
 *     <li>Targets: only players that are skulled and standing in a safe zone. When the player leaves the zone,
 *     loses the skull, dies or logs out the guard drops the target and walks back to its post.</li>
 *     <li>Single combat is ignored on purpose: every guard that notices the player joins in, and a player that is
 *     already fighting someone else is still a valid target. Guards pile.</li>
 *     <li>Fights: ice barrage (damage + freeze) whenever the player isn't frozen, otherwise melee when adjacent and
 *     barrage when not. A guard that falls behind teleports next to the player.</li>
 * </ul>
 */
public class DMMGuardCombat extends NPCCombat {

    private static final Projectile BARRAGE = new Projectile(DMMGuardConfig.BARRAGE_PROJECTILE, 43, 0, 51, 56, 10, 16, 64).skipTravel();

    @Override
    public void init() {
    }

    /**
     * Targeting
     */

    private static boolean isOutlaw(Player player) {
        return Skull.hasSkull(player) && SafeZone.isSafe(player) && !player.isHidden() && !player.getCombat().isDead();
    }

    @Override
    public boolean canAttack(Entity target) {
        if (isDead() || target == null || target.player == null)
            return false;
        return isOutlaw(target.player);
    }

    /**
     * No single combat: the guard never cares who else is fighting the player.
     */
    @Override
    public boolean multiCheck(Entity target) {
        return true;
    }

    @Override
    protected Entity findAggressionTarget() {
        Player closest = npc.localPlayers().stream()
                .filter(this::canAggro)
                .min(Comparator.comparingInt(p -> Misc.getDistance(npc.getPosition(), p.getPosition())))
                .orElse(null);
        if (closest != null)
            npc.forceText(DMMGuardConfig.AGGRO_SHOUT);
        return closest;
    }

    @Override
    protected boolean canAggro(Player player) {
        return canAttack(player) && Misc.getDistance(npc.getPosition(), player.getPosition()) <= DMMGuardConfig.AGGRO_RANGE;
    }

    /**
     * Movement
     */

    @Override
    public void follow() {
        // close in on frozen players for the melee, otherwise stay at casting range
        follow(target.isFrozen() ? 1 : DMMGuardConfig.CAST_RANGE - 2);
    }

    @Override
    public void reset() {
        super.reset();
        returnToPost();
    }

    private void returnToPost() {
        if (npc.isRemoved() || npc.spawnPosition == null)
            return;
        Position post = npc.spawnPosition;
        if (Misc.getDistance(npc.getPosition(), post) > DMMGuardConfig.RETURN_TELEPORT_RANGE)
            npc.getMovement().teleport(post);
        else
            DumbRoute.route(npc, post.getX(), post.getY());
    }

    /**
     * Attacking
     */

    @Override
    public boolean attack() {
        if (target == null || target.player == null)
            return false;

        if (Misc.getDistance(npc.getPosition(), target.getPosition()) > DMMGuardConfig.TELEPORT_RANGE) {
            catchUp();
            return false;
        }

        if (!target.isFrozen()) {
            if (!withinDistance(DMMGuardConfig.CAST_RANGE))
                return false;
            iceBarrage();
            return true;
        }
        if (withinDistance(1)) {
            basicAttack(DMMGuardConfig.MELEE_ANIMATION, AttackStyle.CRUSH, DMMGuardConfig.MELEE_MAX_HIT);
            return true;
        }
        if (withinDistance(DMMGuardConfig.CAST_RANGE)) {
            iceBarrage();
            return true;
        }
        return false;
    }

    private void catchUp() {
        Position pos = target.getPosition();
        // never land on the player's own tile, a guard standing inside its target can't cast
        npc.getMovement().teleport(new Position(pos.getX() + ThreadLocalRandom.current().nextInt(-1, 2), pos.getY() + 1, pos.getZ()));
        npc.forceText(DMMGuardConfig.TELEPORT_SHOUT);
    }

    private void iceBarrage() {
        Entity victim = target;
        npc.animate(DMMGuardConfig.CAST_ANIMATION);
        int delay = BARRAGE.send(npc, victim);
        Hit hit = new Hit(npc, AttackStyle.MAGIC, null).randDamage(DMMGuardConfig.BARRAGE_MAX_HIT).clientDelay(delay);
        hit.afterPostDamage(e -> {
            boolean splash = hit.isBlocked();
            victim.graphics(splash ? DMMGuardConfig.BARRAGE_SPLASH_GFX : DMMGuardConfig.BARRAGE_HIT_GFX, splash ? 124 : 0, 0);
            if (!splash)
                freeze(victim);
        });
        victim.hit(hit);
    }

    private void freeze(Entity victim) {
        if (victim.hasFreezeImmunity())
            return;
        int seconds = DMMGuardConfig.FREEZE_SECONDS;
        if (victim.player != null && victim.player.getPrayer().isActive(Prayer.PROTECT_FROM_MAGIC))
            seconds /= 2;
        victim.freeze(seconds, npc);
        if (victim.player != null) {
            victim.player.sendMessage("You have been frozen by the guards!");
            victim.player.getPacketSender().sendWidget(Widget.BARRAGE, seconds);
        }
    }

}
