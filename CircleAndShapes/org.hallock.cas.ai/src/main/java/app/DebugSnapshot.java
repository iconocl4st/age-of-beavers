package app;

import client.state.EntityTracker;
import common.state.EntityReader;
import common.state.spec.ResourceType;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class DebugSnapshot {

    public static class EntityInformation {
        public EntityReader rider;
        public EntityReader riding;
        public final TreeSet<String> assignments = new TreeSet<>();
        public final TreeSet<EntityReader> garrisoners = new TreeSet<>(EntityReader.COMPARATOR);
        public final TreeSet<EntityReader> garrisoned = new TreeSet<>(EntityReader.COMPARATOR);

    }

    public int population;
    public int numShiftable;
    public int desiredNumTransportWagons;
    public boolean desiresMoreDropoffWagons;
    public int storageSpace;

    public final Map<ResourceType, Integer> desiredResources = new TreeMap<>(ResourceType.COMPARATOR);
    public final Map<ResourceType, Integer> collectedResources = new TreeMap<>(ResourceType.COMPARATOR);
    public final Map<ResourceType, Integer> desiredAllocations = new TreeMap<>(ResourceType.COMPARATOR);

    public final TreeMap<EntityReader, EntityInformation> information = new TreeMap<>(EntityReader.COMPARATOR);

    void addEntityTracker(EntityTracker tracker) {
        this.information.clear();
        for (EntityReader reader : tracker.getTracked()) {
            EntityInformation info = new EntityInformation();
            info.rider = reader.getRider();
            info.riding = reader.getRiding();
            info.garrisoned.addAll(reader.getGarrisoned());
            this.information.put(reader, info);
        }
    }

    void addTickProcessingState(TickProcessingState state) {
        this.desiredResources.clear();
        this.desiredResources.putAll(state.desiredResources);
        this.collectedResources.clear();
        this.collectedResources.putAll(state.collectedResources);
        this.population = state.population;
        this.storageSpace = state.storageSpace;
    }

    void addPersistentState(PersistentAiState state) {
        addAssignment(state.hunters.entities, "hunters");
        addAssignment(state.berryGatherers.entities, "berry gatherer");
        addAssignment(state.lumberJacks.entities, "lumber jack");
        addAssignment(state.stoneGatherers.entities, "stone gatherer");
        addAssignment(state.dropOffs.entities, "drop off");
        addAssignment(state.transporters.entities, "transporter");
        addAssignment(state.garrisoners.entities, "garrisoner");
        addAssignment(state.constructionZones, "construction zone");
        addAssignment(state.constructionWorkers, "construction worker");
        addAssignment(state.allocatedCarts, "allocated cart");
        for (Map.Entry<EntityReader, EntityReader> entry : state.garrisonServicers.entrySet())
            information.get(entry.getKey()).garrisoners.add(entry.getValue());
        this.desiredNumTransportWagons = state.desiredNumTransportWagons;
        this.desiresMoreDropoffWagons = state.desiresMoreDropoffWagons;
    }

    private void addAssignment(Collection<EntityReader> assignment, String name) {
        for (EntityReader reader : assignment)
            this.information.get(reader).assignments.add(name);
    }
}
