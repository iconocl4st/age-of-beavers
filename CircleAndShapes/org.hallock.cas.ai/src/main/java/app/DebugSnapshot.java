package app;

import app.assign.Assignments;
import client.state.EntityTracker;
import common.state.EntityReader;
import common.state.spec.ResourceType;

import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class DebugSnapshot {
    public static class EntityInformation {
        public EntityReader rider;
        public EntityReader riding;
        public String assignment;
        public int assignmentPriority;
        public final TreeSet<EntityReader> garrisoners = new TreeSet<>(EntityReader.COMPARATOR);
        public final TreeSet<EntityReader> garrisoned = new TreeSet<>(EntityReader.COMPARATOR);

    }

    public int population;
    public int numShiftable;
    public boolean desiresMoreTransportWagons;
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

    void addAssignments(Assignments assignments) {
        assignments.addDebugAssignments(this);
    }

    void addGoals(Goals goals) {
        this.desiresMoreTransportWagons = goals.wantsMoreTransportWagons;
        this.desiresMoreDropoffWagons = goals.wantsMoreDropoffWagons;
    }
}
