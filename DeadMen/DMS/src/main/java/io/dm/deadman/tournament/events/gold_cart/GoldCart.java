package io.dm.deadman.tournament.events.gold_cart;

import io.dm.api.utils.Random;
import io.dm.cache.Color;
import io.dm.deadman.tournament.events.DMMEvent;
import io.dm.deadman.tournament.events.reward.EventRewards;
import io.dm.deadman.tournament.events.reward.RewardTier;
import io.dm.model.World;
import io.dm.model.entity.npc.NPC;
import io.dm.model.map.Bounds;
import io.dm.model.map.Position;

/**
 * GOLD CART EVENT  -  "The Caravan"
 * -----------------------------------
 * A goblin courier (id 5208) carries tournament gold along the main trade road
 * from Edgeville to Varrock West Gate.  The cart is NOT a combat target:
 *
 *   - It does NOT retaliate to attacks.
 *   - It is IMMUNE to freezes (resetFreeze every tick).
 *   - At each junction waypoint it wanders in a small area for several seconds,
 *     giving players time to catch up — or set up an ambush.
 *
 * When the cart reaches its destination, every nearby player receives a HIGH-tier
 * reward.  If the goblin is killed en route the event simply ends with no reward,
 * making escort vs. intercept a live decision.
 *
 * Game Design Bible: Pillar 3 (always something at stake), Pillar 4 (forces
 * players to converge on a moving objective), Pillar 5 (respectful of time —
 * route length is ~3 minutes at walking speed).
 */
public class GoldCart extends DMMEvent {

    private static final int GOBLIN_ID    = 5208;
    /** Ticks the goblin lingers at each junction (~12 seconds). */
    private static final int LOITER_TICKS = 20;
    /** Tile radius for the end-of-route reward. */
    private static final int REWARD_RADIUS = 8;

    // -------------------------------------------------------------------------
    // Route: Edgeville bank  →  south Edgeville  →  Barbarian Village
    //        →  east crossroads  →  Varrock West Gate (FINISH)
    // -------------------------------------------------------------------------
    private static final Position START = new Position(3086, 3498, 0);

    private static final Position[] WAYPOINTS = {
        new Position(3086, 3476, 0), // junction 0 — south Edgeville
        new Position(3082, 3423, 0), // junction 1 — Barbarian Village crossroads
        new Position(3107, 3422, 0), // junction 2 — east Barb Village junction
        new Position(3185, 3435, 0), // FINISH     — Varrock West Gate
    };

    private static final String[] JUNCTION_NAMES = {
        "south Edgeville",
        "Barbarian Village",
        "the east crossroads",
        "Varrock West Gate",
    };

    private NPC     goblin;
    private boolean running;

    private int     waypointIndex = 0;
    private boolean loitering     = false;
    private int     loiterTicks   = 0;
    private boolean routeSet      = false; // guard: true once routeAbsolute has been called

    @Override
    public void spawn() {
        startTime  = System.currentTimeMillis();
        lastUpdate = startTime;

        goblin = new NPC(GOBLIN_ID).spawn(START);
        if (goblin.getCombat() != null) {
            goblin.getCombat().setAllowRetaliate(false);
        }

        waypointIndex = 0;
        loitering     = false;
        running       = true;

        broadcast(Color.GOLD.wrap("[Tournament] ") + "A "
                + Color.YELLOW.wrap("Gold Cart")
                + " has departed " + Color.GREEN.wrap("Edgeville bank")
                + "! Escort it to " + Color.GREEN.wrap("Varrock")
                + " — or intercept it along the trade road.");

        routeTo(WAYPOINTS[0]);
        routeSet = true;

        World.startEvent(e -> {
            while (running) {
                tick();
                e.delay(1);
            }
        });
    }

    private void tick() {
        if (eventFinished) return;

        // Goblin might be killed by players; clean up gracefully.
        if (goblin == null || goblin.isRemoved()) {
            broadcast(Color.RED.wrap("[Gold Cart] ") + "The Gold Cart has been destroyed! No reward this time.");
            despawn();
            return;
        }

        // Freeze immunity and forced non-retaliation, every single tick.
        goblin.resetFreeze();
        if (goblin.getCombat() != null && goblin.getCombat().getTarget() != null) {
            goblin.getCombat().setTarget(null);
        }

        lastUpdate = System.currentTimeMillis();

        // ---- State: LOITERING at a junction ----
        if (loitering) {
            loiterTicks--;
            // Wander randomly within ±2 tiles of the current junction.
            Position junc = WAYPOINTS[waypointIndex];
            int wx = junc.getX() + Random.get(4) - 2;
            int wy = junc.getY() + Random.get(4) - 2;
            goblin.getRouteFinder().routeAbsolute(wx, wy);

            if (loiterTicks <= 0) {
                loitering = false;
                waypointIndex++;
                if (waypointIndex >= WAYPOINTS.length) {
                    finish();
                } else {
                    broadcast(Color.YELLOW.wrap("[Gold Cart] ")
                            + "The cart is moving again, heading for "
                            + Color.GREEN.wrap(JUNCTION_NAMES[waypointIndex]) + ".");
                    routeTo(WAYPOINTS[waypointIndex]);
                    routeSet = true;
                }
            }
            return;
        }

        // ---- State: MOVING toward current waypoint ----
        if (routeSet && goblin.getMovement().isAtDestination()) {
            routeSet = false;

            if (waypointIndex == WAYPOINTS.length - 1) {
                finish();
            } else {
                broadcast(Color.YELLOW.wrap("[Gold Cart] ")
                        + "The cart has stopped at "
                        + Color.GREEN.wrap(JUNCTION_NAMES[waypointIndex])
                        + " to rest. Next stop: "
                        + Color.GREEN.wrap(JUNCTION_NAMES[waypointIndex + 1]) + ".");
                loitering   = true;
                loiterTicks = LOITER_TICKS;
            }
        }
    }

    private void finish() {
        broadcast(Color.GOLD.wrap("[Tournament] ") + "The "
                + Color.YELLOW.wrap("Gold Cart")
                + " has arrived at " + Color.GREEN.wrap("Varrock West Gate")
                + "! Nearby players receive a reward.");

        Position dest = WAYPOINTS[WAYPOINTS.length - 1];
        World.players.forEach(p -> {
            if (p == null || p.dead()) return;
            if (p.getPosition().isWithinDistance(dest, REWARD_RADIUS)) {
                EventRewards.giveReward(p, RewardTier.HIGH);
                p.sendMessage(Color.GOLD.wrap("[Gold Cart] ")
                        + "You escorted the cart — and earned your reward!");
            }
        });

        despawn();
    }

    @Override
    public void update() {
        // All logic is in the tick-accurate World.startEvent loop started in spawn().
    }

    @Override
    public void despawn() {
        running = false;
        if (goblin != null && !goblin.isRemoved()) {
            goblin.remove();
        }
        goblin       = null;
        eventFinished = true;
    }

    @Override
    public Bounds bounds() {
        return new Bounds(START, 2);
    }

    private void routeTo(Position pos) {
        goblin.getRouteFinder().routeAbsolute(pos.getX(), pos.getY());
    }

    private void broadcast(String message) {
        World.players.forEach(p -> p.sendMessage(message));
    }
}
