package io.dm.deadman.tournament.events.breach;

import io.dm.cache.Color;
import io.dm.deadman.tournament.events.DMMEvent;
import io.dm.model.World;
import io.dm.model.entity.npc.NPC;
import io.dm.model.map.Bounds;
import io.dm.model.map.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * BREACH EVENT  -  "The Incursion"
 * ---------------------------------
 * OSRS DMM-inspired escalating wave assault in the Wilderness (lvl 38, Chaos Altar).
 * Five waves of escalating difficulty, culminating in a boss encounter.
 *
 * Wave 1 — The Undead Tide     : Skeletons + Ghouls (warm-up; lots of them)
 * Wave 2 — Demonic Vanguard    : Bloodvelds + Hellhounds (ramp-up)
 * Wave 3 — Elite Demonkin      : Black Demons + Abyssal Demons
 * Wave 4 — Nightmare Creatures : Dark Beasts + Greater Nechryael
 * Wave 5 — BOSS               : The Dread Warden (DMM Giant Demon, id 2053)
 *
 * Once all NPCs in a wave are dead, a short break plays before the next wave
 * spawns.  Kill speed is rewarded — waves don't stall on a timer, they stall on
 * the last NPC dying.
 *
 * Game Design Bible: Pillar 3 (always something at stake), Pillar 4 (forces
 * all players into one wilderness spot), Pillar 2 (skill beats gear — the boss
 * only dies if the player group co-operates and dodges the mechanics).
 */
public class Breach extends DMMEvent {

    // -------------------------------------------------------------------------
    // Wave data: each entry is { npcId, spawnCount }.
    // -------------------------------------------------------------------------
    private static final int[][] WAVE_1 = {{924, 4}, {289, 3}};   // Skeleton, Ghoul
    private static final int[][] WAVE_2 = {{484, 3}, {104, 2}};   // Bloodveld, Hellhound
    private static final int[][] WAVE_3 = {{240, 2}, {415, 2}};   // Black Demon, Abyssal Demon
    private static final int[][] WAVE_4 = {{4005, 1}, {7278, 2}}; // Dark Beast, Greater Nechryael
    private static final int     GIANT_DEMON_ID = 2053;            // The Dread Warden

    // -------------------------------------------------------------------------
    // Location: Chaos Altar area, Wilderness ~level 38 (multi-combat).
    // -------------------------------------------------------------------------
    private static final Position CENTER = new Position(3238, 3869, 0);
    private static final int      RADIUS = 10;

    /** Ticks to wait after a wave is cleared before the next one spawns (~18s). */
    private static final int WAVE_BREAK_TICKS = 30;

    private Bounds  breachBounds;
    private boolean running;
    private int     currentWave   = 0;
    private int     breakTicks    = 20; // initial grace period before wave 1

    private final List<NPC> activeNpcs = new ArrayList<>();

    @Override
    public void spawn() {
        startTime = System.currentTimeMillis();
        lastUpdate = startTime;
        running    = true;

        int cx = CENTER.getX(), cy = CENTER.getY(), z = CENTER.getZ();
        breachBounds = new Bounds(cx - RADIUS, cy - RADIUS, cx + RADIUS, cy + RADIUS, z);

        broadcast(Color.DARK_RED.wrap("[BREACH] ") + Color.RED.wrap("Demonic forces")
                + " are tearing through the Wilderness near the "
                + Color.RED.wrap("Chaos Altar") + " (lvl 38)! "
                + "Repel the incursion before the Dread Warden arrives.");

        World.startEvent(e -> {
            while (running && !eventFinished) {
                tick();
                e.delay(1);
            }
        });
    }

    private void tick() {
        lastUpdate = System.currentTimeMillis();

        // Remove NPCs that have been fully killed and removed from the world.
        activeNpcs.removeIf(npc -> npc == null || npc.isRemoved());

        if (!activeNpcs.isEmpty()) return; // wave still in progress

        // All clear — count down to the next wave.
        if (breakTicks > 0) {
            breakTicks--;
            return;
        }

        // Spawn the next wave.
        currentWave++;
        spawnWave(currentWave);
    }

    private void spawnWave(int wave) {
        switch (wave) {
            case 1:
                spawnNpcs(WAVE_1);
                broadcast(Color.DARK_RED.wrap("[BREACH] ") + "Wave 1: "
                        + Color.RED.wrap("The undead tide") + " pours through the breach!");
                break;

            case 2:
                spawnNpcs(WAVE_2);
                broadcast(Color.DARK_RED.wrap("[BREACH] ") + "Wave 2: "
                        + Color.RED.wrap("The demonic vanguard") + " charges forward!");
                break;

            case 3:
                spawnNpcs(WAVE_3);
                broadcast(Color.DARK_RED.wrap("[BREACH] ") + "Wave 3: "
                        + Color.RED.wrap("Elite demonkin") + " flood the area — hold the line!");
                break;

            case 4:
                spawnNpcs(WAVE_4);
                broadcast(Color.DARK_RED.wrap("[BREACH] ") + "Wave 4: "
                        + Color.RED.wrap("Nightmare creatures") + " crawl from the abyss. Nearly there!");
                break;

            case 5:
                // Boss wave — single Giant Demon; its deathEndListener handles the reward.
                NPC boss = new NPC(GIANT_DEMON_ID).spawn(CENTER);
                activeNpcs.add(boss);
                broadcast(Color.DARK_RED.wrap("[BREACH] ") + Color.RED.wrap("THE DREAD WARDEN")
                        + " has arrived! Slay it for "
                        + Color.GOLD.wrap("GOD-TIER") + " rewards!");
                // No break-tick reset — the boss dying triggers the end via its own listener.
                return;

            default:
                // All waves cleared (shouldn't reach here normally; boss listener handles end).
                broadcast(Color.GOLD.wrap("[BREACH] ") + "The incursion has been repelled!");
                despawn();
                return;
        }

        breakTicks = WAVE_BREAK_TICKS; // arm the countdown for the NEXT wave
    }

    private void spawnNpcs(int[][] waveData) {
        for (int[] entry : waveData) {
            int npcId = entry[0];
            int count = entry[1];
            for (int i = 0; i < count; i++) {
                Position pos = breachBounds.randomPosition();
                NPC spawned = new NPC(npcId).spawn(pos);
                // Ensure the NPC is removed on death rather than respawning.
                spawned.deathEndListener = (entity, killer, killHit) -> spawned.remove();
                activeNpcs.add(spawned);
            }
        }
    }

    @Override
    public void update() {
        // All logic is in the tick-accurate World.startEvent loop started in spawn().
    }

    @Override
    public void despawn() {
        running = false;
        for (NPC npc : activeNpcs) {
            if (npc != null && !npc.isRemoved()) npc.remove();
        }
        activeNpcs.clear();
        eventFinished = true;
    }

    @Override
    public Bounds bounds() {
        return breachBounds != null ? breachBounds : new Bounds(CENTER, RADIUS);
    }

    private void broadcast(String message) {
        World.players.forEach(p -> p.sendMessage(message));
    }
}
