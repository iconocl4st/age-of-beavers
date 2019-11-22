package app.algo;

import common.DebugGraphics;
import common.state.EntityId;
import common.util.DPoint;

import java.awt.*;
import java.util.*;
import java.util.List;

public class KMeans {
    // TODO: cache, maybe?
    private final HashMap<EntityId, Assignment> resources = new HashMap<>();
    private final HashMap<EntityId, Point> storageLocations = new HashMap<>();

    private final int[] xSums;
    private final int[] ySums;
    private final DPoint[] centers;
    private final int[] sizes;
    private final int k;
    private final Random random;
    private double totalCost;
    private boolean initialized;

    public KMeans(Random random, int k) {
        this.k = k;
        xSums = new int[k];
        ySums = new int[k];
        centers = new DPoint[k];
        sizes = new int[k];
        this.random = random;
    }

    public void setStorageLocation(EntityId entityId, Point location) {
        storageLocations.put(entityId, location);
    }

    public void setResourceLocation(EntityId entityId, Point location) {
        resources.put(entityId, new Assignment(location));
    }

    private void initializeZeroSets() {
        ArrayList<Assignment> ouch = new ArrayList<>(resources.values());
        for (int i = 0; i < k; i++) {
            if (sizes[i] != 0) {
                continue;
            }
            Point p = ouch.get(random.nextInt(ouch.size())).location;
            centers[i] = new DPoint(p.x, p.y);
        }

        initialized = true;
    }

    public double update(int iterations) {
        if (resources.isEmpty() || k + storageLocations.size() == 0) return 0.0;

        if (!initialized)
            initializeZeroSets();
        boolean change = true;
        for (int i = 0; i < iterations && change; i++) {
            change = false;
            change |= assign();
            change |= recenter();
            initializeZeroSets();
        }
        return totalCost;
    }

    private boolean recenter() {
        for (int i = 0; i < k; i++) {
            xSums[i] = 0;
            ySums[i] = 0;
            sizes[i] = 0;
        }
        for (Assignment assignment : resources.values()) {
            if (assignment.center < 0) {
                continue;
            }
            ++sizes[assignment.center];
            Point p = assignment.location;
            xSums[assignment.center] += p.x;
            ySums[assignment.center] += p.y;
        }

        boolean change = false;
        for (int i = 0; i < k; i++) {
            DPoint current;
            if (sizes[i] <= 0) {
                current = new DPoint();
            } else {
                current = new DPoint(xSums[i] / sizes[i], ySums[i] / sizes[i]);
            }
            if (current.x == centers[i].x && current.y == centers[i].y) {
                continue;
            }
            centers[i] = current;
            change = true;
        }
        return change;
    }

    private boolean assign() {
        boolean change = false;
        totalCost = 0.0;
        for (Assignment assignment : resources.values()) {
            int minIdx = -1;
            double minDist = Double.MAX_VALUE;
            for (int j = 0; j < k; j++) {
                DPoint center = centers[j];
                double dx = center.x - assignment.location.x;
                double dy = center.y - assignment.location.y;
                double d = Math.sqrt(dx * dx + dy * dy);
                if (d < minDist) {
                    minIdx = j;
                    minDist = d;
                }
            }
            EntityId nearestStorage = null;
            for (Map.Entry<EntityId, Point> entry : storageLocations.entrySet()) {
                double dx = entry.getValue().x - assignment.location.x;
                double dy = entry.getValue().y - assignment.location.y;
                double d = Math.sqrt(dx * dx + dy * dy);
                if (d < minDist) {
                    minDist = d;
                    nearestStorage = entry.getKey();
                }
            }
            totalCost += minDist;
            if (nearestStorage != null) {
                if (assignment.storageLocation == null || !assignment.storageLocation.equals(nearestStorage)) {
                    assignment.storageLocation = nearestStorage;
                    change = true;
                }
            } else if (assignment.center != minIdx) {
                assignment.center = minIdx;
                change = true;
            }
        }
        return change;
    }

    public List<DebugGraphics> getDebugGraphics() {
        if (resources.isEmpty() || k + storageLocations.size() == 0) return Collections.emptyList();
        List<DebugGraphics> debug = new ArrayList<>(k + storageLocations.size());
        HashMap<EntityId, Integer> indices = new HashMap<>();
        for (int i = 0; i < k; i++)
            debug.add(new DebugGraphics(centers[i]));
        for (Map.Entry<EntityId, Point> entry : storageLocations.entrySet()) {
            indices.put(entry.getKey(), debug.size());
            debug.add(new DebugGraphics(new DPoint(entry.getValue())));
        }
        for (Assignment assignment : resources.values()) {
            debug.get(assignment.center < 0 ? indices.get(assignment.storageLocation) : assignment.center).list.add(assignment.location);
        }
        return debug;
    }

    public void reset() {
        resources.clear(); // this is the one that hurts
        storageLocations.clear();
    }

    public int getK() {
        return k;
    }

    public double totalCost() {
        return totalCost;
    }

    public DPoint[] getCenters() {
        return centers;
    }

    private static class Assignment {
        Point location;
        int center = -1;
        EntityId storageLocation;

        Assignment(Point location) {
            this.location = location;
        }
    }
}
