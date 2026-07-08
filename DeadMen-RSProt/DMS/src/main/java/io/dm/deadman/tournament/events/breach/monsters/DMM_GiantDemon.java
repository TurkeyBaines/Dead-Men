package io.dm.deadman.tournament.events.breach.monsters;

import io.dm.api.utils.Random;
import io.dm.cache.Color;
import io.dm.deadman.tournament.events.breach.BreachNPC;
import io.dm.deadman.tournament.events.reward.EventRewards;
import io.dm.deadman.tournament.events.reward.RewardTier;
import io.dm.model.World;
import io.dm.model.combat.AttackStyle;
import io.dm.model.combat.Hit;
import io.dm.model.entity.player.Player;
import io.dm.model.map.Position;
import io.dm.model.map.Projectile;

/**
 * DMM GIANT DEMON  -  "The Dread Warden"
 * ---------------------------------------
 * Custom boss NPC for the Breach event (NPC id 2053).  The Dread Warden is a
 * scaled-up Black Demon with three distinct attack styles and two special mechanics
 * that kick in at 50% HP.
 *
 * Attack rotation (Phase 1):
 *   - Melee crush if target is adjacent (40% chance when in range).
 *   - Rock hurl (ranged) or Demonic hellfire (magic) otherwise - 50/50 split.
 *
 * Phase 2 (≤50% HP):
 *   - Announces enrage; all damage doubled.
 *   - Every 6 attacks: Shockwave — AOE crush hit to all players within 4 tiles.
 *
 * Death: GOD-tier reward to the killing blow's player; global server broadcast.
 */
public class DMM_GiantDemon extends BreachNPC {

    private static final int GFX_RANGED    = 451;  // ground impact (Jad ranged)
    private static final int GFX_MAGIC_HIT = 157;  // hit splat on target
    private static final int GFX_AOE_BURN  = 66;   // fire column for shockwave AoE

    private static final Projectile MAGIC_PROJECTILE =
            new Projectile(448, 128, 31, 2, 10, 8, 16, 32);

    private int attackCount = 0;
    private boolean enraged  = false;

    @Override
    public void init() {
        super.init();
        npc.deathEndListener = (entity, killer, killHit) -> {
            if (killer != null && killer.player != null) {
                EventRewards.giveReward(killer.player, RewardTier.GOD);
            }
            World.players.forEach(p -> p.sendMessage(
                    Color.GOLD.wrap("[Breach] ") + Color.RED.wrap("The Dread Warden")
                    + " has been slain! The breach seals shut."));
            npc.remove();
        };
    }

    @Override
    public void follow() {
        follow(10);
    }

    @Override
    public boolean attack() {
        if (target == null) return false;

        checkEnrage();
        attackCount++;

        int maxDmg = enraged ? info.max_damage * 2 : info.max_damage;

        // Phase 2 shockwave: every 6 attacks when enraged
        if (enraged && attackCount % 6 == 0) {
            shockwave();
            return true;
        }

        // Melee preference when target is adjacent
        if (withinDistance(1) && Random.rollPercent(40)) {
            npc.animate(info.attack_animation);
            target.hit(new Hit(npc, AttackStyle.CRUSH).randDamage(maxDmg));
            return true;
        }

        if (!withinDistance(10)) return false;

        if (Random.rollPercent(50)) {
            rangedAttack(maxDmg);
        } else {
            magicAttack(maxDmg);
        }
        return true;
    }

    private void rangedAttack(int maxDmg) {
        npc.animate(info.attack_animation);
        npc.addEvent(e -> {
            e.delay(1);
            if (target == null) return;
            World.sendGraphics(GFX_RANGED, 0, 0, target.getPosition());
            Hit hit = new Hit(npc, AttackStyle.RANGED).randDamage(maxDmg);
            hit.postDamage(t -> t.graphics(GFX_MAGIC_HIT));
            target.hit(hit);
        });
    }

    private void magicAttack(int maxDmg) {
        npc.animate(info.attack_animation);
        npc.addEvent(e -> {
            e.delay(2);
            if (target == null) return;
            int delay = MAGIC_PROJECTILE.send(npc, target);
            Hit hit = new Hit(npc, AttackStyle.MAGIC).randDamage(maxDmg).clientDelay(delay);
            hit.postDamage(t -> t.graphics(GFX_MAGIC_HIT));
            target.hit(hit);
        });
    }

    /** AOE shockwave: hits every player within 4 tiles for moderate crush damage. */
    private void shockwave() {
        npc.animate(info.attack_animation);
        npc.localPlayers().forEach(p ->
                p.sendMessage(Color.RED.wrap("The Dread Warden SLAMS the ground — RUN!")));
        npc.addEvent(e -> {
            e.delay(1);
            Position origin = npc.getPosition();
            for (Player p : World.players) {
                if (p == null || p.dead()) continue;
                if (!p.getPosition().isWithinDistance(origin, 4)) continue;
                World.sendGraphics(GFX_AOE_BURN, 0, 0, p.getPosition());
                p.hit(new Hit(npc, AttackStyle.CRUSH).randDamage(info.max_damage));
            }
        });
    }

    private void checkEnrage() {
        if (enraged) return;
        if (npc.getMaxHp() > 0 && npc.getHp() <= npc.getMaxHp() / 2) {
            enraged = true;
            npc.localPlayers().forEach(p ->
                    p.sendMessage(Color.RED.wrap(
                            "[Breach] The Dread Warden ENRAGES at half health — damage doubled!")));
        }
    }
}
