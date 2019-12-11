package client.event.supply;

import common.state.EntityReader;
import common.state.spec.GameSpec;
import common.state.spec.ResourceType;
import common.state.sst.sub.Load;
import common.util.Immutable;
import common.util.MapUtils;

import java.util.*;

public class StorageTracker {
    private final Object sync = new Object();
    private final HashMap<ResourceType, Integer> collectedResources = new HashMap<>();
    private final HashMap<ResourceType, Integer> availableWeight = new HashMap<>();
    private final Immutable.ImmutableList<ResourceType> resourceTypes;

    private HashMap<EntityReader, TrackedStorage> storageFacilities = new HashMap<>();

    StorageTracker(GameSpec spec) {
        this.resourceTypes = spec.resourceTypes;
    }

    public boolean update(EntityReader entity) {
        synchronized (sync) {
            StorageState previousStorageState = getStorageState();
            remove(entity);
            add(entity);
            StorageState currentStorageState = getStorageState();
            boolean r = !previousStorageState.equals(currentStorageState);
            if (r) {
                System.out.println("The previous storage state:");
                System.out.println(previousStorageState);
                System.out.println("The new storage state:");
                System.out.println(currentStorageState);
            }
            return r;
        }
    }

    public boolean currentlyHave(ResourceType resourceType) {
        return collectedResources.getOrDefault(resourceType, 0) > 0;
    }

    public boolean currentlyHaveSpaceFor(ResourceType resourceType) {
        return availableWeight.getOrDefault(resourceType, 0) > 0;
    }

    private void remove(EntityReader reader) {
        TrackedStorage remove = storageFacilities.remove(reader);
        if (remove == null) return;
        MapUtils.subtract(collectedResources, remove.contributingResources);
        MapUtils.subtract(availableWeight, remove.contributingResources);
    }

    private void add(EntityReader entity) {
        if (entity.noLongerExists() || !entity.getType().containsClass("storage"))
            return;
        TrackedStorage tracked = new TrackedStorage();
        tracked.contributingTotalWeight = entity.getAmountOfResourceAbleToAccept();
        tracked.contributingResources = entity.getExtraResources();
        storageFacilities.put(entity, tracked);
        MapUtils.add(collectedResources, tracked.contributingResources);
        MapUtils.add(availableWeight, tracked.contributingTotalWeight);
    }


    private static final class TrackedStorage {
        Map<ResourceType, Integer> contributingTotalWeight;
        Map<ResourceType, Integer> contributingResources;
    }

    private StorageState getStorageState() {
        StorageState storageState = new StorageState();
        for (ResourceType resourceType : resourceTypes) {
            ResourceStorageState rss = new ResourceStorageState();
            rss.hasRoomFor = availableWeight.getOrDefault(resourceType, 0) > 0;
            rss.has = collectedResources.getOrDefault(resourceType, 0) > 0;
            storageState.storageStates.put(resourceType, rss);
        }
        return storageState;
    }

    private static final class StorageState {
        HashMap<ResourceType, ResourceStorageState> storageStates = new HashMap<>();

        public boolean equals(Object other) {
            if (!(other instanceof StorageState)) {
                return false;
            }
            StorageState ss = (StorageState) other;
            if (storageStates.size() != ss.storageStates.size()) return false;
            if (!storageStates.keySet().containsAll(ss.storageStates.keySet())) return false;
            for (Map.Entry<ResourceType, ResourceStorageState> entry : storageStates.entrySet()) {
                if (!ss.storageStates.get(entry.getKey()).equals(entry.getValue()))
                    return false;
            }
            return true;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            List<Map.Entry<ResourceType, ResourceStorageState>> l = new LinkedList<>(storageStates.entrySet());
            l.sort(Comparator.comparing(e -> e.getKey().name));
            for (Map.Entry<ResourceType, ResourceStorageState> entry : l) {
                builder.append(entry.getKey().name).append(':').append(entry.getValue()).append(';');
            }
            return builder.toString();
        }
    }

    private static final class ResourceStorageState {
        boolean has;
        boolean hasRoomFor;

        public boolean equals(Object other) {
            if (!(other instanceof ResourceStorageState)) return false;
            ResourceStorageState rss = (ResourceStorageState) other;
            return has == rss.has && hasRoomFor == rss.hasRoomFor;
        }

        public String toString() {
            return "[" + has + "," + hasRoomFor + "]";
        }
    }
}
