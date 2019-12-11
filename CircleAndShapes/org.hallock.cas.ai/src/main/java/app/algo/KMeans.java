package app.algo;

import common.DebugGraphics;
import common.state.EntityId;
import common.state.spec.ResourceType;
import common.util.DPoint;

import java.util.*;

public class KMeans {
    // TODO: cache, maybe?
    private final HashMap<EntityId, Assignment> resources = new HashMap<>();
    private final HashMap<EntityId, Storage> storageLocations = new HashMap<>();

    private final double[] xSums;
    private final double[] ySums;
    private final DPoint[] centers;
    private final int[] sizes;
    private final int k;
    private final Random random;
    private double totalCost;
    private boolean initialized;

    public KMeans(Random random, int k) {
        this.k = k;
        xSums = new double[k];
        ySums = new double[k];
        centers = new DPoint[k];
        sizes = new int[k];
        this.random = random;
    }

    public void setStorageLocation(EntityId entityId, DPoint location, Set<ResourceType> resourcesAccepting) {
        storageLocations.put(entityId, new Storage(location, resourcesAccepting));
    }

    public void setResourceLocation(EntityId entityId, DPoint location, ResourceType resourceType) {
        resources.put(entityId, new Assignment(location, resourceType));
    }

    private void initializeZeroSets() {
        ArrayList<Assignment> ouch = new ArrayList<>(resources.values());
        for (int i = 0; i < k; i++) {
            if (sizes[i] != 0) {
                continue;
            }
            centers[i] = ouch.get(random.nextInt(ouch.size())).location;
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
            if (assignment.center < 0)
                continue;
            ++sizes[assignment.center];
            DPoint p = assignment.location;
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
                // what if the storage cart can't accept anymore?
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
            for (Map.Entry<EntityId, Storage> entry : storageLocations.entrySet()) {
                Storage storage = entry.getValue();
                if (!storage.resources.contains(assignment.resourceType))
                    continue;
                double dx = storage.location.x - assignment.location.x;
                double dy = storage.location.y - assignment.location.y;
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
        for (Map.Entry<EntityId, Storage> entry : storageLocations.entrySet()) {
            indices.put(entry.getKey(), debug.size());
            debug.add(new DebugGraphics(entry.getValue().location));
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

    private static class Storage {
        DPoint location;
        Set<ResourceType> resources;

        Storage(DPoint location, Set<ResourceType> resources) {
            this.location = location;
            this.resources = resources;
        }
    }

    private static class Assignment {
        ResourceType resourceType;
        DPoint location;
        int center = -1;
        EntityId storageLocation;

        Assignment(DPoint location, ResourceType resourceType) {
            this.location = location;
            this.resourceType = resourceType;
        }
    }
}
