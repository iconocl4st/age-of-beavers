package app;

import app.algo.KMeans;
import client.ai.ai2.Gather;
import client.ai.ai2.Move;
import client.ai.ai2.Produce;
import client.ai.ai2.TransportAi;
import common.algo.AStar;
import common.algo.ConnectedSet;
import common.msg.Message;
import common.state.EntityReader;
import common.state.Player;
import common.state.spec.CreationSpec;
import common.state.spec.ResourceType;
import common.state.sst.sub.ConstructionZone;
import common.state.sst.sub.capacity.Prioritization;
import common.util.DPoint;
import common.util.MapUtils;
import common.util.query.GridLocationQuerier;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

public class TickProcessingState {

    private final PlayerAiContext context;

    int population;

    HashMap<ResourceType, Integer> peopleOnResource = new HashMap<>();
    HashMap<ResourceType, Integer> desiredResources = new HashMap<>();
    HashMap<ResourceType, Integer> collectedResources = new HashMap<>();
    int storageSpace;

    LinkedList<EntityReader> garrisonsToService = new LinkedList<>();
    LinkedList<EntityReader> constructionToService = new LinkedList<>();

//    LinkedList<DPoint> storageLocations = new LinkedList<>();

    HashSet<String> currentlyBuilding = new HashSet<>();
    HashSet<String> currentlyOwned = new HashSet<>();

    PeoplePuller peoplePuller;

    KMeans[] resourceClusters;

    public TickProcessingState(PlayerAiContext context, PeoplePuller puller) {
        this.context = context;
        peoplePuller = puller;

        resourceClusters = new KMeans[2];
    }

    void reset(PersistentAiState state) {
        storageSpace = 0;
        population = 0;
        desiredResources.clear();
        collectedResources.clear();
        garrisonsToService.clear();
        constructionToService.clear();
        peopleOnResource.clear();
        peoplePuller.clear();
        currentlyBuilding.clear();
//        storageLocations.clear();
        if (resourceClusters[0] == null || resourceClusters[0].getK() != state.dropOffs.size()) {
            resourceClusters[0] = new KMeans(context.random, state.dropOffs.size());
            resourceClusters[1] = new KMeans(context.random, state.dropOffs.size() + 2);
        } else {
            for (KMeans resourceCluster : resourceClusters) resourceCluster.reset();
        }
    }

    private void moveDropOffToDesiredLocation(EntityReader entity, PersistentAiState persistentAiState, EntityReader riding) {
        DPoint desiredLocation = persistentAiState.desiredDropOffLocations.get(riding);
        if (desiredLocation == null)
            return;
        if (desiredLocation.distanceTo(entity.getLocation()) < 5)
            return;
        Point nearestEmptyTile = ConnectedSet.findNearestEmptyTile(context.clientGameState.gameState.gameSpec, desiredLocation.toPoint(), context.clientGameState.gameState.getOccupancyView(context.clientGameState.currentPlayer));
        if (nearestEmptyTile == null)
            return;
        AStar.PathSearch path = GridLocationQuerier.findPath(context.clientGameState.gameState, entity.entityId, new DPoint(nearestEmptyTile), context.clientGameState.currentPlayer);
        if (path == null)
            return;
        context.clientGameState.aiManager.set(entity, new Move(entity, new DPoint(nearestEmptyTile)));
//        context.msgQueue.send(new Message.RequestAction(entity.entityId, new Action.MoveSeq(path.path)));
    }

    void update(EntityReader entity, PersistentAiState persistentAiState) {
        currentlyOwned.add(entity.getType().name);

        client.ai.ai2.AiTask currentAi = context.clientGameState.aiManager.get(entity);
        if (entity.getType().name.equals("wagon") && currentAi == null) {
            EntityReader riding = entity.getRiding();
            if (riding != null && persistentAiState.transporters.contains(riding)) {
                context.clientGameState.aiManager.set(entity, new TransportAi(entity));
            } else if (riding != null && persistentAiState.dropOffs.contains(riding)) {
                moveDropOffToDesiredLocation(entity, persistentAiState, riding);
            }
        }

        if (currentAi instanceof Gather) {
            EntityReader currentResource = ((Gather) currentAi).getCurrentResource();
            DPoint location;
            if (currentResource != null && (location = currentResource.getCenterLocation()) != null) {
                for (KMeans resourceCluster : resourceClusters)
                    resourceCluster.setResourceLocation(entity.entityId, location.toPoint());
            }
        }

        boolean isHuman = entity.getType().name.equals("human");
        if (isHuman) population += 1;

        if (persistentAiState.isIdle(entity)) {
            if (isHuman) {
                peoplePuller.addIdle(entity);
            } else if (!entity.getType().canCreate.isEmpty()) {
                if (entity.getOwner().equals(context.clientGameState.currentPlayer)) {
                    CreationSpec spec = persistentAiState.getRecommendedCreation(entity.getType().canCreate);
                    if (spec != null)
                        context.clientGameState.aiManager.set(entity, new Produce(entity, spec));
                } else if (entity.getOwner().equals(Player.GAIA) && !persistentAiState.garrisonServicers.containsKey(entity)) {
                    garrisonsToService.add(entity);
                }
            }
        }

        if (entity.getType().containsClass("can-garrison-others")) {
            if (persistentAiState.getDesiredNumGarrisons(entity) > entity.getNumGarrisonedUnits() && persistentAiState.garrisonServicers.get(entity) == null) {
                garrisonsToService.add(entity);
            }
        }

        if (entity.getType().containsClass("storage")) {
            MapUtils.add(collectedResources, entity.getCarrying().quantities);
            storageSpace += entity.getCapacity().getMaximumWeightHoldable();
            DPoint centerLocation = entity.getCenterLocation();
            if (centerLocation != null) {
                for (KMeans resourceCluster : resourceClusters) {
                    resourceCluster.setStorageLocation(entity.entityId, centerLocation.toPoint());
                }
            }
        }

        // TODO: setResourceLocation demands...
        if (currentAi instanceof Produce) {
            Produce createAi = (Produce) currentAi;
            Map<ResourceType, Integer> creationResources = MapUtils.multiply(MapUtils.copy(createAi.getCreating().createdType.requiredResources), 2);
            checkDemands(entity, creationResources);
            MapUtils.add(desiredResources, creationResources);
        }


        ConstructionZone constructionZone = entity.getConstructionZone();
        if (constructionZone != null) {
            currentlyBuilding.add(constructionZone.constructionSpec.resultingStructure.name);
            Map<ResourceType, Integer> missingConstructionResources = entity.getMissingConstructionResources();
            if (MapUtils.sum(missingConstructionResources) == 0) {
                if (!persistentAiState.constructionZones.contains(entity))
                    constructionToService.add(entity);
            } else {
                MapUtils.add(desiredResources, missingConstructionResources);
            }
        }
    }

    private void checkDemands(EntityReader entity, Map<ResourceType, Integer> creationResources) {
        for (Map.Entry<ResourceType, Integer> entry : creationResources.entrySet()) {
            Prioritization prioritization = entity.getCapacity().getPrioritization(entry.getKey());
            int desiredMinimum = Math.min(prioritization.maximumAmount, entry.getValue());
            if (prioritization.desiredAmount < desiredMinimum) {
                context.clientGameState.actionRequester.getWriter().send(new Message.SetDesiredCapacity(entity.entityId, entry.getKey(), 1, desiredMinimum, prioritization.maximumAmount));
            }
        }
    }

    public Map<ResourceType, Integer> getMissingResources() {
        return MapUtils.positivePart(MapUtils.subtract(MapUtils.copy(desiredResources), collectedResources));
    }

    public void determineShiftableVillagers(Map<ResourceType, Integer> desiredAllocations) {
        peoplePuller.addExcess(MapUtils.positivePart(MapUtils.subtract(MapUtils.copy(peopleOnResource), desiredAllocations)).entrySet());
    }

    public LinkedList<Map.Entry<ResourceType, Integer>> determineMissingAllocations(Map<ResourceType, Integer> desiredAllocations) {
        return new LinkedList<>(MapUtils.positivePart(MapUtils.subtract(MapUtils.copy(desiredAllocations), peopleOnResource)).entrySet());
    }

    public Map<ResourceType, Integer> determineExcessResources() {
        return MapUtils.positivePart(MapUtils.subtract(MapUtils.copy(collectedResources), desiredResources));
    }

    public double determineStoragePercentage() {
        int sum = 0;
        for (Map.Entry<ResourceType, Integer> entry : collectedResources.entrySet()) {
            sum += entry.getValue() * entry.getKey().weight;
        }
        return sum / (double) storageSpace;
    }
}
