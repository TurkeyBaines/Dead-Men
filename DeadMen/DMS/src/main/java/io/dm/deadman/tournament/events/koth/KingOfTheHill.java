package io.dm.deadman.tournament.events.koth;

import io.dm.api.utils.Random;
import io.dm.cache.Color;
import io.dm.deadman.tournament.events.DMMEvent;
import io.dm.deadman.tournament.events.reward.EventRewards;
import io.dm.deadman.tournament.events.reward.RewardTier;
import io.dm.model.World;
import io.dm.model.combat.Hit;
import io.dm.model.entity.player.Player;
import io.dm.model.map.Bounds;
import io.dm.model.map.Position;
import io.dm.model.map.object.GameObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * KING OF THE HILL  -  "The Beacon"
 * ---------------------------------
 * A beacon ignites at a contested overworld location and slowly charges. Players bank
 * "charge" by standing in the zone - but ONLY while alone:
 *
 *   - Exactly ONE player in the zone  -> that player accrues charge (and the beacon's
 *     total charge ticks up).
 *   - TWO OR MORE players in the zone  -> contested: nobody's charge ticks (you have to
 *     clear the hill to make progress).
 *   - The beacon randomly hurls LIGHTNING at the area: a warning orb drops on a tile and,
 *     three ticks later, lightning strikes it for heavy damage. Stand on the orb and you
 *     get hit - move off it and you're safe.
 *
 * When the beacon finishes charging (or the event times out), players are ranked by their
 * share of the total charge:
 *   - 1st place  -> a big (GOD-tier) reward.
 *   - 2nd & 3rd  -> a smaller (HIGH-tier) reward.
 *
 * Game Design Bible fit: Pillar 3 (always something at stake - a single objective plus a
 * live hazard), Pillar 4 (forces teams to collide over one spot), Pillar 2 (you earn the
 * crown by winning fights and dodging mechanics, never by buying gear).
 *
 * All timing is in game ticks (1 tick = 600ms) for precise hazard telegraphing.
 */
public class KingOfTheHill extends DMMEvent {

    private static final int BEACON_LIT = 29748;   // lit brazier  (charging)
    private static final int BEACON_UNLIT = 29747; // unlit brazier (spent)

    private static final int GFX_WARNING = 1406;   // orb that drops on the doomed tile
    private static final int GFX_LIGHTNING = 1666; // the strike itself

    /** Solo-occupied ticks needed to fully charge the beacon (~108s of banked time). */
    private static final int TARGET_CHARGE_TICKS = 180;
    /** Hard cap so an uncontested hill can't run forever (8 minutes). */
    private static final int MAX_TICKS = 800;
    /** Ticks between the warning orb landing and the strike. */
    private static final int WARNING_TICKS = 3;
    /** How often to announce the current standings. */
    private static final int BROADCAST_EVERY = 25;
    /** Random lightning damage ceiling. */
    private static final int LIGHTNING_MAX = 18;

    private Spawns location;
    private Bounds captureZone;
    private GameObject beacon;

    private boolean running;
    private int ticks;
    private int totalCharge;
    private final Map<Player, Integer> contribution = new LinkedHashMap<>();

    // Pending lightning strike (-1 == none scheduled).
    private Position strikeTile;
    private int strikeAtTick = -1;
    private int nextStrikeTick;

    @Override
    public void spawn() {
        startTime = System.currentTimeMillis();
        lastUpdate = startTime;

        location = Spawns.values()[Random.get(Spawns.values().length - 1)];
        Position c = location.center;
        captureZone = new Bounds(c.getX() - 2, c.getY() - 2, c.getX() + 2, c.getY() + 2, c.getZ());

        beacon = new GameObject(BEACON_LIT, c, 10, 0);
        beacon.spawn();

        nextStrikeTick = 8; // brief grace before the first bolt
        running = true;

        broadcast(Color.GOLD.wrap("[Tournament] ") + "A " + Color.RED.wrap("Beacon")
                + " has ignited " + Color.BLUE.wrap(location.text)
                + "! Hold the hill ALONE to charge it - but beware its lightning.");

        // Tick-accurate driver loop (the framework's update() is too coarse for the hazard).
        World.startEvent(e -> {
            while (running) {
                tick();
                e.delay(1);
            }
        });
    }

    private void tick() {
        if (eventFinished || beacon == null)
            return;

        ticks++;

        // 1. Who is on the hill right now (alive only).
        List<Player> inZone = new ArrayList<>();
        for (Player p : World.players) {
            if (p == null || p.dead())
                continue;
            if (captureZone.inBounds(p.getPosition()))
                inZone.add(p);
        }

        // 2. Charge accrual - solo only. Two or more = contested, nothing ticks.
        if (inZone.size() == 1) {
            Player only = inZone.get(0);
            contribution.merge(only, 1, Integer::sum);
            totalCharge++;
        }

        // 3. The hazard.
        handleLightning(inZone);

        // 4. Periodic standings.
        if (ticks % BROADCAST_EVERY == 0 && totalCharge > 0) {
            int pct = totalCharge * 100 / TARGET_CHARGE_TICKS;
            Player leader = leader();
            String who = leader == null ? "nobody (contested)" : Color.GREEN.wrap(leader.getName());
            broadcast(Color.GOLD.wrap("[Beacon] ") + "Charge " + Color.RED.wrap(Math.min(100, pct) + "%")
                    + " - leader: " + who + ".");
        }

        // 5. End conditions.
        if (totalCharge >= TARGET_CHARGE_TICKS) {
            finish(Color.GOLD.wrap("[Tournament] ") + "The Beacon " + Color.BLUE.wrap(location.text)
                    + " is fully charged!");
        } else if (ticks >= MAX_TICKS) {
            finish(Color.GOLD.wrap("[Tournament] ") + "The Beacon " + Color.BLUE.wrap(location.text)
                    + " flickers and dies out...");
        }
    }

    private void handleLightning(List<Player> inZone) {
        // Resolve a pending strike.
        if (strikeAtTick != -1) {
            if (ticks >= strikeAtTick) {
                World.sendGraphics(GFX_LIGHTNING, 0, 0, strikeTile);
                for (Player p : World.players) {
                    if (p == null || p.dead())
                        continue;
                    if (p.getPosition().equals(strikeTile)) {
                        p.hit(new Hit().randDamage(LIGHTNING_MAX));
                        p.sendMessage(Color.RED.wrap("Lightning from the Beacon strikes you!"));
                    }
                }
                strikeTile = null;
                strikeAtTick = -1;
                nextStrikeTick = ticks + 5 + Random.get(6); // 5-11 tick cooldown
            } else {
                // Keep the warning orb visible across the telegraph window.
                World.sendGraphics(GFX_WARNING, 0, 0, strikeTile);
            }
            return;
        }

        // Schedule a fresh strike at a random occupant's tile.
        if (!inZone.isEmpty() && ticks >= nextStrikeTick) {
            Player victim = inZone.get(Random.get(inZone.size() - 1));
            Position at = victim.getPosition();
            strikeTile = new Position(at.getX(), at.getY(), at.getZ()); // snapshot - they can dodge
            strikeAtTick = ticks + WARNING_TICKS;
            World.sendGraphics(GFX_WARNING, 0, 0, strikeTile);
        }
    }

    private void finish(String reason) {
        broadcast(reason);

        List<Map.Entry<Player, Integer>> ranked = new ArrayList<>(contribution.entrySet());
        ranked.sort(Comparator.comparingInt(Map.Entry<Player, Integer>::getValue).reversed());

        if (ranked.isEmpty() || totalCharge <= 0) {
            broadcast(Color.RED.wrap("Nobody managed to charge the Beacon. No reward this time."));
            despawn();
            return;
        }

        String[] places = {"1st", "2nd", "3rd"};
        int announced = Math.min(3, ranked.size());
        for (int i = 0; i < announced; i++) {
            Player p = ranked.get(i).getKey();
            int share = ranked.get(i).getValue() * 100 / totalCharge;
            broadcast(Color.GOLD.wrap("[Beacon] ") + places[i] + ": "
                    + Color.GREEN.wrap(p.getName()) + " (" + share + "% of the charge)");
        }

        // 1st -> big reward, 2nd & 3rd -> smaller reward.
        EventRewards.giveReward(ranked.get(0).getKey(), RewardTier.GOD);
        for (int i = 1; i < announced; i++) {
            EventRewards.giveReward(ranked.get(i).getKey(), RewardTier.HIGH);
        }

        despawn();
    }

    @Override
    public void update() {
        // Intentionally empty: all logic runs in the tick-accurate loop started in spawn().
    }

    @Override
    public void despawn() {
        running = false;
        if (beacon != null) {
            Position pos = beacon.getPosition();
            beacon.remove();
            beacon = new GameObject(BEACON_UNLIT, pos, 10, 0);
            beacon.spawn();
            beacon = null;
        }
        eventFinished = true;
    }

    @Override
    public Bounds bounds() {
        return captureZone;
    }

    private Player leader() {
        Player best = null;
        int bestTicks = 0;
        for (Map.Entry<Player, Integer> e : contribution.entrySet()) {
            if (e.getValue() > bestTicks) {
                bestTicks = e.getValue();
                best = e.getKey();
            }
        }
        return best;
    }

    private void broadcast(String message) {
        World.players.forEach(p -> p.sendMessage(message));
    }

    /** Contested, open overworld spots that naturally pull teams together. */
    public enum Spawns {
        EDGEVILLE(new Position(3087, 3496, 0), "in Edgeville"),
        BARB_VILLAGE(new Position(3082, 3420, 0), "in Barbarian Village"),
        VARROCK_SQUARE(new Position(3213, 3422, 0), "in Varrock Square"),
        AL_KHARID(new Position(3293, 3185, 0), "outside Al-Kharid Palace"),
        CASTLE_WARS(new Position(2440, 3090, 0), "near Castle Wars"),
        FALADOR(new Position(2965, 3380, 0), "in Falador"),
        DRAYNOR(new Position(3083, 3253, 0), "in Draynor Village");

        final Position center;
        final String text;

        Spawns(Position center, String text) {
            this.center = center;
            this.text = text;
        }
    }
}
