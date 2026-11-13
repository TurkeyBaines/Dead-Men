package io.dm.deadman.content.guard;

import io.dm.data.impl.npcs.npc_combat;
import io.dm.model.combat.AttackStyle;

/**
 * Everything about the safe zone guards that is meant to be tuned: which npc they are, where they stand and how
 * they fight. The guard's combat is built from this in code ({@link #combatInfo()}), so the npc id does not need an
 * entry in the npc combat json.
 */
public final class DMMGuardConfig {

    private DMMGuardConfig() {
    }

    /**
     * TODO: placeholder id, set to the real guard npc. The animation ids below belong to that npc's model.
     */
    public static final int NPC_ID = 2661;

    /**
     * Guard posts, one {x, y, z} per guard. TODO: fill in. Empty = no guards spawn on boot
     * (use {@code ::tourn guard spawn} to drop a test guard on your own tile).
     */
    public static final int[][] SPAWNS = {
    };

    /** Animation used for the ice barrage cast / the melee swing. */
    public static final int CAST_ANIMATION = 2269;
    public static final int MELEE_ANIMATION = 422;

    /** Ice barrage graphics (these are the spell's, not the npc's). */
    public static final int BARRAGE_PROJECTILE = 368;
    public static final int BARRAGE_HIT_GFX = 369;
    public static final int BARRAGE_SPLASH_GFX = 85;

    public static final int BARRAGE_MAX_HIT = 30;
    public static final int MELEE_MAX_HIT = 20;

    /** Freeze duration in the same unit as {@code Entity.freeze}; halved by Protect from Magic. */
    public static final int FREEZE_SECONDS = 20;

    /** Ticks between attacks. */
    public static final int ATTACK_TICKS = 4;

    /** A guard casts from at most this many tiles. */
    public static final int CAST_RANGE = 8;

    /** A guard further than this from its skulled target teleports next to it. */
    public static final int TELEPORT_RANGE = 12;

    /** How far away (in tiles) a guard notices a skulled player. */
    public static final int AGGRO_RANGE = 16;

    /** A guard that ends up further than this from its post teleports back instead of walking. */
    public static final int RETURN_TELEPORT_RANGE = 15;

    public static final String AGGRO_SHOUT = "You're not welcome here, outlaw!";
    public static final String TELEPORT_SHOUT = "Don't you run away from me!";

    static npc_combat.Info combatInfo() {
        npc_combat.Info info = new npc_combat.Info();
        info.ids = new int[] {NPC_ID};
        info.hitpoints = 1000;          // never actually damaged, guards can't be attacked
        info.aggressive_level = 1;      // any non-zero value: the guard decides who to aggro itself
        info.max_damage = MELEE_MAX_HIT;
        info.attack_style = AttackStyle.MAGIC;
        info.attack = info.strength = info.defence = info.ranged = 99;
        info.magic = 150;
        info.magic_attack = 200;
        info.attack_ticks = ATTACK_TICKS;
        info.attack_animation = MELEE_ANIMATION;
        info.defend_animation = -1;
        info.death_animation = -1;
        info.spawn_animation = -1;
        info.respawn_ticks = 50;
        info.death_ticks = 0;
        return info;
    }

}
