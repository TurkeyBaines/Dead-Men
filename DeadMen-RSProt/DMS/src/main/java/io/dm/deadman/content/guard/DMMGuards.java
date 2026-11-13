package io.dm.deadman.content.guard;

import io.dm.model.map.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Spawns and tracks the safe zone guards.
 */
public final class DMMGuards {

    private static final List<DMMGuard> GUARDS = new ArrayList<>();

    private DMMGuards() {
    }

    /**
     * Spawns every guard post from {@link DMMGuardConfig#SPAWNS}.
     */
    public static void spawnAll() {
        for (int[] post : DMMGuardConfig.SPAWNS)
            spawn(post[0], post[1], post[2]);
    }

    public static DMMGuard spawn(Position position) {
        return spawn(position.getX(), position.getY(), position.getZ());
    }

    public static DMMGuard spawn(int x, int y, int z) {
        DMMGuard guard = new DMMGuard();
        guard.spawn(x, y, z);
        GUARDS.add(guard);
        return guard;
    }

    /**
     * Removes every guard, returns how many there were.
     */
    public static int clear() {
        int count = GUARDS.size();
        for (DMMGuard guard : GUARDS)
            guard.remove();
        GUARDS.clear();
        return count;
    }

    public static int count() {
        GUARDS.removeIf(DMMGuard::isRemoved);
        return GUARDS.size();
    }

}
