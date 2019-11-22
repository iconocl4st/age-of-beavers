package app;

import app.algo.KMeans;
import client.ai.ActionRequester;
import client.ai.ai2.ConstructAll;
import client.ai.ai2.Gather;
import client.ai.ai2.Hunt;
import client.ai.ai2.WhileWithinProximity;
import client.state.ClientGameState;
import common.DebugGraphics;
import common.msg.Message;
import common.state.EntityReader;
import common.state.spec.CreationSpec;
import common.state.spec.EntitySpec;
import common.state.spec.ResourceType;
import common.state.sst.GameState;
import common.util.DPoint;
import common.util.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

class PlayerAiImplementation {
    private final PlayerAiContext context;

    private PersistentAiState persistentState;
    private TickProcessingState tickState;

    PlayerAiImplementation(PlayerAiContext playerAiContext) {
        this.context = playerAiContext;
        persistentState = new PersistentAiState(playerAiContext);
        tickState = new TickProcessingState(playerAiContext, new PeoplePuller(persistentState));
    }


    private GameState state() {
        return clientGameState().gameState;
    }
    private ClientGameState clientGameState() {
        return context.clientGameState;
    }

    // need to addIdle the initial carts...
    synchronized DebugSnapshot updateActions(ActionRequester actionRequester) {
        // get on a horse
        // build a corral
        // build a stable
        // build another brothel
        // build weapons
        // fight
        // build more storage
        // detect when resources run out
        // when the drop off does not need to move, the people should gather resources...

        // why does the brothel have only 100 maximum resources?
        // fresh spawns take a while to gather?


        persistentState.verifyCounts();
        tickState.reset(persistentState);
        persistentState.update(tickState.peopleOnResource);

        persistentState.assignDropOffCarts(tickState.resourceClusters[0].getCenters());

        for (EntityReader entityReader : clientGameState().entityTracker.getTracked()) {
            Object sync = entityReader.getSync();
            if (sync == null) continue;
            synchronized (sync) {
                if (entityReader.noLongerExists()) {
                    continue;
                }
                tickState.update(entityReader, persistentState);
            }
        }

        for (KMeans resourceCluster : tickState.resourceClusters) {
            resourceCluster.update(20);
        }

        double cost1 = tickState.resourceClusters[0].totalCost();
        double cost2 = tickState.resourceClusters[1].totalCost();
        EntityReader gatherCart = persistentState.getShiftToGatherCart(cost1, cost2);

        Set<EntityReader> wagons = persistentState.getShiftsToTransport(tickState.population, tickState);
        int numShiftingToConstruction = persistentState.getRequiredToShiftToConstruction(tickState.constructionToService.size(), tickState.peoplePuller);


        int numShiftableHumans = (
                + MapUtils.sum(tickState.peopleOnResource)
                + tickState.peoplePuller.idles.size()
                - wagons.size()
                - tickState.garrisonsToService.size()
                - numShiftingToConstruction
                - (gatherCart == null ? 0 : 1)
        );

        if (numShiftableHumans < 0) {
            throw new RuntimeException("Not enough humans");
        }

        Map<ResourceType, Integer> missingResources = tickState.getMissingResources();
        Map<ResourceType, Integer> desiredAllocations = MapUtils.getDesired(numShiftableHumans, missingResources);

        tickState.determineShiftableVillagers(desiredAllocations);

        for (EntityReader wagon : wagons) {
            EntityReader next = tickState.peoplePuller.next();
            pull(actionRequester, next);
            clientGameState().aiManager.set(next, WhileWithinProximity.createBeRidden(next, wagon));
            persistentState.transporters.assigned(next);
            persistentState.allocatedCarts.add(wagon);
        }

        if (gatherCart != null) {
            EntityReader next = tickState.peoplePuller.next();
            pull(actionRequester, next);
            clientGameState().aiManager.set(next, WhileWithinProximity.createBeRidden(next, gatherCart));
            persistentState.dropOffs.assigned(next);
            persistentState.allocatedCarts.add(gatherCart);
        }

        for (EntityReader reader : tickState.garrisonsToService) {
            EntityReader pull = pull(actionRequester, tickState.peoplePuller.next());
            persistentState.garrisoners.assigned(pull);
            clientGameState().aiManager.set(pull, WhileWithinProximity.createGarrison(pull, reader));
            persistentState.garrisonServicers.put(reader, pull);
        }

        persistentState.constructionZones.addAll(tickState.constructionToService);
        for (int i = 0; i < numShiftingToConstruction; i++) {
            EntityReader pull = pull(actionRequester, tickState.peoplePuller.next());
            clientGameState().aiManager.set(pull, new ConstructAll(pull, persistentState.getNextConstructionZone()));
            persistentState.constructionWorkers.add(pull);
        }

        for (Map.Entry<ResourceType, Integer> missingEntry : tickState.determineMissingAllocations(desiredAllocations)) {
            if (missingEntry.getValue() <= 1)
                continue;
            for (int i = 0; i < missingEntry.getValue(); i++) {
                persistentState.addTo(pull(actionRequester, tickState.peoplePuller.next()), missingEntry.getKey(), this);
            }
        }

        // if people are still idle...
        EntityReader idle;
        while ((idle = tickState.peoplePuller.getNextIdle()) != null) {
            persistentState.addTo(idle, state().gameSpec.getResourceType("food"), this);
        }


        if (tickState.determineStoragePercentage() > 0.5 && !tickState.currentlyBuilding.contains("storage-yard")) {
            EntitySpec buildingType = state().gameSpec.getUnitSpec("storage-yard");
            DPoint location = context.utils.getSpaceForBuilding(buildingType);
            if (location != null)
                context.clientGameState.actionRequester.getWriter().send(new Message.PlaceBuilding(buildingType, (int) location.x, (int) location.y));
        }

        if ((persistentState.desiresMoreDropoffWagons || persistentState.desiredNumTransportWagons > persistentState.transporters.size())
                && !tickState.currentlyBuilding.contains("carpenter-shop")
                && !tickState.currentlyOwned.contains("carpenter-shop")) {
            EntitySpec buildingType = state().gameSpec.getUnitSpec("carpenter-shop");
            DPoint location = context.utils.getSpaceForBuilding(buildingType);
            if (location != null)
                context.clientGameState.actionRequester.getWriter().send(new Message.PlaceBuilding(buildingType, (int) location.x, (int) location.y));
        }


        // spend extra resources...

        Map<ResourceType, Integer> excessResources = tickState.determineExcessResources();
        if (excessResources.getOrDefault(state().gameSpec.getResourceType("food"), 0) > 500 && !tickState.currentlyBuilding.contains("brothel")) {
            EntitySpec buildingType = state().gameSpec.getUnitSpec("brothel");
            DPoint location = context.utils.getSpaceForBuilding(buildingType);
            if (location != null)
                context.clientGameState.actionRequester.getWriter().send(new Message.PlaceBuilding(buildingType, (int) location.x, (int) location.y));
        }


        DebugSnapshot snapshot = new DebugSnapshot();
        snapshot.addEntityTracker(clientGameState().entityTracker);
        snapshot.addPersistentState(persistentState);
        snapshot.addTickProcessingState(tickState);
        snapshot.numShiftable = numShiftableHumans;
        snapshot.desiredAllocations.clear();
        snapshot.desiredAllocations.putAll(desiredAllocations);
        return snapshot;
    }

//    private void transport(EntityReader pull, EntityReader wagon) {
//        if (wagon != null) {
//            clientGameState().aiManager.startAi(pull.entityId, new BeRidden(clientGameState(), pull, wagon));
//            persistentState.transporters.assigned(pull);
//            return;
//        }
//        clientGameState().aiManager.startAi(pull.entityId, new TransportAi(clientGameState(), pull));
//        persistentState.transporters.assigned(pull);
//    }

    private int getNumGarrisonsDesired(EntityReader entity) {
        switch (entity.getType().name) {
            case "brothel": return 1;
            case "storage-yard": return 0;
        }
        return 0;
    }

    private EntityReader pull(ActionRequester requester, EntityReader reader) {
        EntityReader rider = reader.getRider();
        if (rider != null && rider.getType().name.equals("wagon")) {
            requester.setUnitActionToDismount(rider);
        }
        persistentState.remove(reader);
        return reader;
    }


    void hunt(EntityReader entity) {
        if (!entity.getType().containsClass("hunter"))
            throw new RuntimeException("Can't hunt");
        EntitySpec deer = state().gameSpec.getUnitSpec("deer");
        clientGameState().aiManager.set(entity, new Hunt(entity, null, deer));
    }

    void gather(EntityReader entity, String name) {
        if (!entity.getType().containsClass("gatherer"))
            throw new RuntimeException("Can't gather");
        EntitySpec type = state().gameSpec.getNaturalResource(name);
        if (type == null) throw new NullPointerException("Cannot find " + name);
        clientGameState().aiManager.set(entity, new Gather(entity, null, type));
    }

    private CreationSpec getBestCreation(Set<CreationSpec> canCreate) {
        return canCreate.iterator().next();
    }

    void setDebugGraphics() {
        List<DebugGraphics> debugGraphics = tickState.resourceClusters[0].getDebugGraphics();
        if (debugGraphics == null || debugGraphics.isEmpty()) return;
        synchronized (DebugGraphics.byPlayer) {
            DebugGraphics.byPlayer.put(context.clientGameState.currentPlayer, debugGraphics);
        }
    }
}
