package io.dm.model.map;

import io.dm.model.map.object.GameObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * TEMPORARY diagnostic (not part of the migration's permanent tooling) — dumps every real
 * GameObject the loaded cache placed within a square around a coordinate, straight from the
 * already-parsed {@link Region}/{@link Tile} data, so a hardcoded content coordinate can be
 * checked against this exact cache snapshot's ground truth instead of an external reference.
 * Call before {@code PackageLoader.load("io.dm")} runs (i.e. from {@code Server.main}'s
 * {@code afterData} list) so the dump reflects raw cache state, unmutated by any content class's
 * own static-init side effects.
 */
public final class RegionObjectDumper {

    private RegionObjectDumper() {
    }

    public static void dump(String fileName, int centerX, int centerY, int radius) {
        try (PrintWriter out = new PrintWriter(new FileWriter(fileName, false))) {
            out.println("Region object dump around (" + centerX + ", " + centerY + ") radius " + radius);
            int found = 0;
            for (int z = 0; z < 4; z++) {
                for (int x = centerX - radius; x <= centerX + radius; x++) {
                    for (int y = centerY - radius; y <= centerY + radius; y++) {
                        Region region = Region.get(x, y);
                        if (region == null)
                            continue;
                        Tile tile = region.getTile(x, y, z, false);
                        if (tile == null || tile.gameObjects == null)
                            continue;
                        for (GameObject obj : tile.gameObjects) {
                            out.println("z=" + z + " x=" + x + " y=" + y
                                    + " id=" + obj.id + " type=" + obj.type + " dir=" + obj.direction);
                            found++;
                        }
                    }
                }
            }
            out.println("Total objects found: " + found);
            System.out.println("[RegionObjectDumper] wrote " + fileName + " (" + found + " objects)");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
