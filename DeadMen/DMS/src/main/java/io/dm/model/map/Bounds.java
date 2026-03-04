package io.dm.model.map;

import io.dm.api.utils.Random;

import java.awt.*;
import java.util.function.Consumer;

public class Bounds {

    public final int swX, swY;

    public final int neX, neY;

    public final int z;

    private int sides;
    private int[][] points;


    public Bounds(Position position, int range) {
        this(position.getX(), position.getY(), position.getZ(), range);
    }

    public Bounds(Position swPosition, Position nePosition, int z) {
        this(swPosition.getX(), swPosition.getY(), nePosition.getX(), nePosition.getY(), z);
    }

    public Bounds(int x, int y, int z, int range) {
        this(x - range, y - range, x + range, y + range, z);
    }

    public Bounds(int swX, int swY, int neX, int neY, int z) {
        this.swX = swX;
        this.swY = swY;
        this.neX = neX;
        this.neY = neY;
        this.z = z;
    }

    public Bounds(int[][] points, int z) {
        this.z = z;
        this.points = points;
        this.sides = points.length;

        // Compute AABB for compatibility with existing rectangle logic
        int minX = points[0][0], minY = points[0][1];
        int maxX = points[0][0], maxY = points[0][1];

        for (int[] p : points) {
            if (p[0] < minX) minX = p[0];
            if (p[1] < minY) minY = p[1];
            if (p[0] > maxX) maxX = p[0];
            if (p[1] > maxY) maxY = p[1];
        }

        this.swX = minX;
        this.swY = minY;
        this.neX = maxX;
        this.neY = maxY;
    }

    public boolean isPolygon() {
        return points != null;
    }


    public void forEachPos(Consumer<Position> consumer) {
        int minZ, maxZ;
        if(z == -1) {
            minZ = 0;
            maxZ = 3;
        } else {
            minZ = z;
            maxZ = minZ;
        }
        for(int z = minZ; z <= maxZ; z++) {
            for(int x = swX; x <= neX; x++) {
                for(int y= swY; y <= neY; y++)
                    consumer.accept(new Position(x, y, z));
            }
        }
    }

    public boolean inBounds(int x, int y, int z, int range) {
        return !(this.z != -1 && z != this.z) && x >= swX - range && x <= neX + range && y >= swY - range && y <= neY + range;
    }

    public boolean inBounds(int[][] bounds, Position position) {
        boolean inside = false;
        int x = position.getX(), y = position.getY();
        for (int i = 0, j = bounds.length - 1; i < bounds.length; j = i++) {
            if ((bounds[i][1] < y && bounds[j][1] >= y) || (bounds[j][1] < y && bounds[i][1] >= y)) {
                if (bounds[i][0] + (y - bounds[i][1]) / (bounds[j][1] - bounds[i][1]) * (bounds[j][0] - bounds[i][0]) < x) {
                    inside = !inside;
                }
            }
        }
        return inside;
    }

    public boolean inBounds(Position pos) {
        if (this.z != -1 && pos.getZ() != this.z)
            return false;

        int x = pos.getX();
        int y = pos.getY();

        if (!isPolygon()) {
            // Rectangle logic
            return x >= swX && x <= neX && y >= swY && y <= neY;
        }

        // Polygon logic
        boolean inside = false;
        for (int i = 0, j = points.length - 1; i < points.length; j = i++) {
            int xi = points[i][0], yi = points[i][1];
            int xj = points[j][0], yj = points[j][1];

            boolean intersect = ((yi > y) != (yj > y)) &&
                    (x < (xj - xi) * (y - yi) / (double)(yj - yi) + xi);

            if (intersect)
                inside = !inside;
        }
        return inside;
    }


    public boolean intersects(Bounds other) {
        // AABB broad-phase
        if (neX < other.swX || swX > other.neX || neY < other.swY || swY > other.neY)
            return false;

        // If both are rectangles, AABB is enough
        if (!isPolygon() && !other.isPolygon())
            return true;

        // Polygon vs polygon or polygon vs rectangle
        return polygonIntersects(other);
    }

    private boolean polygonIntersects(Bounds other) {

        // If either is not a polygon, treat the rectangle as a 4‑point polygon
        int[][] polyA = this.isPolygon() ? this.points : rectToPoly(this);
        int[][] polyB = other.isPolygon() ? other.points : rectToPoly(other);

        // 1. Check if any edges intersect
        if (edgesIntersect(polyA, polyB))
            return true;

        // 2. Check if one polygon is fully inside the other
        if (pointInPoly(polyA, polyB[0][0], polyB[0][1]))
            return true;

        if (pointInPoly(polyB, polyA[0][0], polyA[0][1]))
            return true;

        return false;
    }

    private int[][] rectToPoly(Bounds b) {
        return new int[][] {
                { b.swX, b.swY },
                { b.neX, b.swY },
                { b.neX, b.neY },
                { b.swX, b.neY }
        };
    }

    private boolean edgesIntersect(int[][] a, int[][] b) {
        for (int i = 0; i < a.length; i++) {
            int[] a1 = a[i];
            int[] a2 = a[(i + 1) % a.length];

            for (int j = 0; j < b.length; j++) {
                int[] b1 = b[j];
                int[] b2 = b[(j + 1) % b.length];

                if (segmentsIntersect(a1, a2, b1, b2))
                    return true;
            }
        }
        return false;
    }

    private boolean segmentsIntersect(int[] p1, int[] p2, int[] p3, int[] p4) {
        int d1 = direction(p3, p4, p1);
        int d2 = direction(p3, p4, p2);
        int d3 = direction(p1, p2, p3);
        int d4 = direction(p1, p2, p4);

        if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
                ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0)))
            return true;

        // Collinear special cases
        if (d1 == 0 && onSegment(p3, p4, p1)) return true;
        if (d2 == 0 && onSegment(p3, p4, p2)) return true;
        if (d3 == 0 && onSegment(p1, p2, p3)) return true;
        if (d4 == 0 && onSegment(p1, p2, p4)) return true;

        return false;
    }

    private int direction(int[] a, int[] b, int[] c) {
        return (b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0]);
    }

    private boolean onSegment(int[] a, int[] b, int[] c) {
        return Math.min(a[0], b[0]) <= c[0] && c[0] <= Math.max(a[0], b[0]) &&
                Math.min(a[1], b[1]) <= c[1] && c[1] <= Math.max(a[1], b[1]);
    }

    private boolean pointInPoly(int[][] poly, int x, int y) {
        boolean inside = false;

        for (int i = 0, j = poly.length - 1; i < poly.length; j = i++) {
            int xi = poly[i][0], yi = poly[i][1];
            int xj = poly[j][0], yj = poly[j][1];

            boolean intersect = ((yi > y) != (yj > y)) &&
                    (x < (xj - xi) * (y - yi) / (double)(yj - yi) + xi);

            if (intersect)
                inside = !inside;
        }

        return inside;
    }


    public Position randomPosition() {
        if (!isPolygon()) {
            return new Position(randomX(), randomY(), z == -1 ? Random.get(0, 3) : z);
        }

        int minX = swX, maxX = neX;
        int minY = swY, maxY = neY;

        while (true) {
            int x = Random.get(minX, maxX);
            int y = Random.get(minY, maxY);
            Position p = new Position(x, y, z);
            if (inBounds(p))
                return p;
        }
    }


    public int randomX() {
        return Random.get(swX, neX);
    }

    public int randomY() {
        return Random.get(swY, neY);
    }

    public Region getRegion() {
        return Region.get(swX, swY);
    }

    /**
     * Misc
     */

    public static Bounds fromRegion(int regionId) {
        return fromRegion(regionId, -1);
    }

    public static Bounds fromRegion(int regionId, int z) {
        int baseRegionX = (regionId >> 8) * 64, baseRegionY = (regionId & 0xff) * 64;
        return new Bounds(baseRegionX, baseRegionY, baseRegionX + 63, baseRegionY + 63, z);
    }

    public static Bounds fromRegions(int... regionIds) {
        Bounds sw = null, ne = null;
        for(int regionId : regionIds) {
            Bounds bounds = fromRegion(regionId);
            if(sw == null || (bounds.swX < sw.swX && bounds.swY < sw.swY))
                sw = bounds;
            if(ne == null || (bounds.neX > ne.neX && bounds.neY > ne.neY))
                ne = bounds;
        }
        return new Bounds(sw.swX, sw.swY, ne.neX, ne.neY, -1);
    }

}
