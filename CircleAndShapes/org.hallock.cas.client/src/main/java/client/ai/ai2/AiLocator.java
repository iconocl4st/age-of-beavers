package client.ai.ai2;

import client.state.ClientGameState;
import client.state.EntityTracker;
import common.state.EntityReader;
import common.state.spec.EntitySpec;
import common.state.spec.ResourceType;
import common.util.query.GridLocationQuerier;
import common.util.query.NearestEntityQuery;
import common.util.query.NearestEntityQueryResults;

import java.util.Set;

public class AiLocator {

    private final EntityTracker tracker;
    private final ClientGameState clientGameState;

    public AiLocator(ClientGameState clilentGameState) {
        this.tracker = clilentGameState.entityTracker;
        this.clientGameState = clilentGameState;
    }


    public EntityReader locateNearestConstructionZone(EntityReader constructionWorker) {
        for (EntityReader entity : tracker.getTracked("constructionZone", constructionWorker.getCenterLocation())) {
            if (entity.getConstructionZone() == null) continue; // needed?
//            max distance?
            return entity;
        }
        return null;
    }

    public Set<EntityReader> getStorageLocations() {
        return tracker.getTrackedByClass("storage");
    }


    public EntityReader locateNearestDropOffSite(EntityReader controlling, ResourceType key, EntityReader avoid) {
        for (EntityReader entity : tracker.getTracked("storage", controlling.getCenterLocation())) {
            if (entity.equals(avoid))
                continue;
            if (!entity.canAccept(key))
                continue;
            return entity;
        }
        return null;
    }

    public EntityReader locateNearestPickupSite(EntityReader controlling, ResourceType key, EntityReader avoid) {
//        if (!controlling.canAccept(resource)) {
//            return AiAttemptResult.Completed;
        for (EntityReader entity : tracker.getTracked("storage", controlling.getCenterLocation())) {
            if (entity.equals(avoid))
                continue;
            if (entity.doesNotHave(key))
                continue;
            return entity;
        }
        return null;
    }

    public EntityReader locateNearestPrey(AiContext aiContext, EntityReader entity, EntitySpec preyType) {
        // throwing away the path...
        NearestEntityQueryResults results = clientGameState.gameState.locationManager.query(
                new NearestEntityQuery(
                        clientGameState.gameState,
                        entity.getLocation(),
                        e -> {
                            EntitySpec type = clientGameState.gameState.typeManager.get(e);
                            return type != null && type.name.equals(preyType.name);
                        },
                        Double.MAX_VALUE,
                        clientGameState.currentPlayer
                )
        );
        if (!results.successful())
            return null;
        return results.getEntity(clientGameState.gameState);
    }

    public EntityReader locateNearestNaturalResource(EntityReader entity, EntitySpec naturalResourceSpec) {
        // throwing away the path...
        NearestEntityQueryResults results = clientGameState.gameState.locationManager.query(new NearestEntityQuery(
                clientGameState.gameState,
                entity.getCenterLocation(),
                GridLocationQuerier.createNonEmptyNaturalResourceFilter(clientGameState.gameState, naturalResourceSpec),
                Double.MAX_VALUE,
                clientGameState.currentPlayer
        ));
        if (!results.successful()) {
            return null;
        }
        return results.getEntity(clientGameState.gameState);
    }
}
