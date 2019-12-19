package app;

import app.assign.AiCheckContext;
import app.assign.Assigner;
import app.assign.UnitAssignment;
import client.ai.ai2.AiTask;
import client.ai.ai2.Gather;
import client.ai.ai2.Produce;
import common.action.Action;
import common.msg.Message;
import common.state.EntityReader;
import common.state.spec.EntityClasses;
import common.state.spec.EntitySpec;
import common.state.spec.ResourceType;
import common.state.sst.sub.ConstructionZone;
import common.state.sst.sub.capacity.Prioritization;
import common.state.sst.sub.capacity.PrioritizedCapacitySpec;
import common.util.DPoint;
import common.util.MapUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TickProcessingState {

    private final PlayerAiContext context;

    public int population;
    public int storageSpace;

    public final HashMap<ResourceType, Integer> desiredResources = new HashMap<>();
    public final HashMap<ResourceType, Integer> collectedResources = new HashMap<>();

    public final HashSet<EntityReader> garrisoners = new HashSet<>();
    public final HashSet<EntityReader> constructionZones = new HashSet<>();

    public final HashMap<String, Integer> currentlyBuilding = new HashMap<>();
    public final HashMap<String, Integer> currentlyOwned = new HashMap<>();

    public static final class GatherLocation { DPoint location; ResourceType resource; }
    public final HashMap<EntityReader, GatherLocation> gatherLocations = new HashMap<>();
    public static final class StorageLocation { DPoint location; Set<ResourceType> resources; };
    public final HashMap<EntityReader, StorageLocation> storageLocations = new HashMap<>();

    TickProcessingState(PlayerAiContext context) {
        this.context = context;
    }

    void reset() {
        storageSpace = 0;
        population = 0;
        desiredResources.clear();
        collectedResources.clear();
        currentlyBuilding.clear();
        currentlyOwned.clear();
        gatherLocations.clear();
    }

    private static void increaseCount(String type, HashMap<String, Integer> counts) {
        counts.put(type, counts.getOrDefault(type, 0) + 1);
    }

    private void addGatherLocation(EntityReader entity, AiTask currentAi) {
        if (!(currentAi instanceof Gather)) return;
        Gather gather = (Gather) currentAi;
        EntityReader currentResource = gather.getCurrentResource();
        if (currentResource == null) return;
        HashSet<ResourceType> gatheringResourceTypes = gather.getGatheringResourceTypes();
        if (gatheringResourceTypes == null) return;
        DPoint center = currentResource.getCenterLocation();
        if (center == null) return;
        for (ResourceType resourceType : gatheringResourceTypes) {
            GatherLocation location = new GatherLocation();
            location.resource = resourceType;
            location.location = center;
            gatherLocations.put(entity, location);
        }
    }

    void update(EntityReader entity, AiCheckContext c) {
        EntitySpec type = entity.getType();
        increaseCount(type.name, currentlyOwned);

        boolean isHuman = type.name.equals("human");
        Action currentAction = entity.getCurrentAction();
        AiTask currentAi = context.clientGameState.aiManager.get(entity);
        UnitAssignment currentAssignment = c.assignments.get(entity);
        boolean idle = currentAi == null && (currentAction == null || currentAction instanceof Action.Idle);


        if (idle) {
            if (currentAssignment == null) {
                Assigner.assignToNothing(c, entity, AiConstants.IDLE_PRIORITY);
            } else {
                currentAssignment.onIdle.isIdle();
            }
        }

        addGatherLocation(entity, currentAi);
        if (isHuman) ++population;

        if (type.containsClass(EntityClasses.GARRISONS_OTHERS))
            garrisoners.add(entity);

        if (type.containsClass(EntityClasses.STORAGE)) {
            MapUtils.add(collectedResources, entity.getCarrying().quantities);
            PrioritizedCapacitySpec capacity = entity.getCapacity();
            storageSpace += capacity.getMaximumWeightHoldable();
            DPoint centerLocation = entity.getCenterLocation();
            if (centerLocation != null) {
                StorageLocation storage = new StorageLocation();
                storage.location = centerLocation;
                storage.resources = entity.getAmountOfResourceAbleToAccept().keySet();
                storageLocations.put(entity, storage);
            }
        }

        // TODO: setResourceLocation demands...
        if (currentAi instanceof Produce) {
            Produce createAi = (Produce) currentAi;
            Map<ResourceType, Integer> creationResources = MapUtils.multiply(MapUtils.copy(createAi.getCreating().requiredResources), AiConstants.MULTIPLY_CREATION_PRODUCE_DEMAND);
            checkDemands(entity, creationResources, AiConstants.PRODUCE_DEMAND_PRIORITY);
            MapUtils.add(desiredResources, creationResources);
        }


        ConstructionZone constructionZone = entity.getConstructionZone();
        if (constructionZone != null) {
            increaseCount(constructionZone.constructionSpec.resultingStructure.name, currentlyBuilding);


//            PrioritizedCapacitySpec capacity = ;
            Map<ResourceType, Integer> creationResources = entity.getCapacity().getMaximumAmounts();

//            Map<ResourceType, Integer> creationResources = constructionZone.constructionSpec.gresultingStructure.requiredResources;
            checkDemands(entity, creationResources, AiConstants.CONSTRUCTION_DEMAND_PRIORITY);

            Map<ResourceType, Integer> missingConstructionResources = entity.getMissingConstructionResources();
            MapUtils.add(desiredResources, missingConstructionResources);

            constructionZones.add(entity);
        }
    }

    private void checkDemands(EntityReader entity, Map<ResourceType, Integer> creationResources, int priority) {
        for (Map.Entry<ResourceType, Integer> entry : creationResources.entrySet()) {
            Prioritization prioritization = entity.getCapacity().getPrioritization(entry.getKey());
            int desiredMinimum = Math.min(prioritization.maximumAmount, entry.getValue());
            if (prioritization.desiredAmount < desiredMinimum || prioritization.priority != priority) {
                context.clientGameState.actionRequester.getWriter().send(new Message.SetDesiredCapacity(entity.entityId, entry.getKey(), priority, desiredMinimum, prioritization.maximumAmount));
            }
        }
    }

    public int getNumberOf(String type) {
        return currentlyBuilding.getOrDefault(type, 0) + currentlyOwned.getOrDefault(type, 0);
    }

    public int getNumberBuilding(String type) {
        return currentlyBuilding.getOrDefault(type, 0);
    }

    public double determineStoragePercentage() {
        int sum = 0;
        for (Map.Entry<ResourceType, Integer> entry : collectedResources.entrySet()) {
            sum += entry.getValue() * entry.getKey().weight;
        }
        return sum / (double) storageSpace;
    }
}
