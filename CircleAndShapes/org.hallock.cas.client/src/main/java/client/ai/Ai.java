package client.ai;

import client.app.ClientContext;
import common.AiEvent;
import client.event.AiEventListener;
import common.Proximity;
import common.state.spec.EntitySpec;
import common.state.spec.ResourceType;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.sst.sub.Load;
import common.util.EntityQueryFilter;
import common.util.GridLocationQuerier;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public abstract class Ai implements AiEventListener {

    enum AiAttemptResult {
        Successful, // action set
        Unsuccessful,  // failure
        NothingDone,
        Completed,
        ;
    }

    protected final ClientContext context;
    protected final EntityReader controlling;

    protected Ai(ClientContext context, EntityId controlling) {
        this.context = context;
        this.controlling = new EntityReader(context.gameState, controlling);
    }

    public abstract String toString();

    @Override
    public void receiveEvent(AiEvent event, ActionRequester ar) {
        if (!event.type.equals(AiEvent.EventType.ActionCompleted) || !event.entity.equals(controlling.entityId))
            return;
        switch (setActions(ar)) {
            case Unsuccessful:
            case Completed:
                context.aiManager.removeAi(controlling.entityId);
                break;
        }
    }

    public abstract AiAttemptResult setActions(ActionRequester ar);

    protected AiAttemptResult dropOffOtherResources(ActionRequester ar, ResourceType resourceType) {
        return dropOffOtherResources(ar, Collections.singleton(resourceType));
    }

    protected AiAttemptResult dropOffOtherResources(ActionRequester ar, Set<ResourceType> resourceTypes) {
        Load load = controlling.getCarrying();
        if (load == null)
            return AiAttemptResult.Unsuccessful;
        for (Map.Entry<ResourceType, Integer> entry : load.quantities.entrySet()) {
            if (resourceTypes.contains(entry.getKey())) {
                continue;
            }
            if (entry.getValue() <= 0) {
                continue;
            }
            return deliverToNearestDropOff(ar, entry.getKey(), controlling.entityId);
        }
        return AiAttemptResult.NothingDone;
    }

    // TODO cleanup, dry
    protected AiAttemptResult deliverToNearestDropOff(ActionRequester ar, ResourceType resource, EntityId avoid) {
        GridLocationQuerier.NearestEntityQueryResults results = findNearestDropOff(resource, avoid);
        if (!results.successful()) {
            System.out.println("No where to leave it.");
            return AiAttemptResult.Unsuccessful;
        }
        if (Proximity.closeEnoughToInteract(context.gameState, controlling.entityId, results.entity)) {
            ar.setUnitActionToDeposit(controlling, results.entity, resource, Integer.MAX_VALUE);
        } else {
            ar.setUnitActionToMove(controlling, results.path);
        }
        return AiAttemptResult.Successful;
    }

    protected GridLocationQuerier.NearestEntityQueryResults findNearestDropOff(final ResourceType resource, final EntityId avoid) {
        EntityQueryFilter filter = entityId -> {
            if (entityId.equals(avoid)) return false;
            EntityReader entity = new EntityReader(context.gameState, entityId);
            if (entity.getType() == null) return false;
            if (entity.isHidden()) return false;
            if (!entity.canAccept(resource)) return false;
            if (!entity.getType().containsClass("storage")) return false;
            return true;
        };

        // TODO: limit max value?
        GridLocationQuerier.NearestEntityQuery query = new GridLocationQuerier.NearestEntityQuery(context.gameState, controlling.getCenterLocation(), filter, Double.MAX_VALUE, context.currentPlayer);
        return context.gameState.locationManager.query(query);
    }

    protected GridLocationQuerier.NearestEntityQueryResults findNearestResource(String type) {
        return context.gameState.locationManager.query(new GridLocationQuerier.NearestEntityQuery(
                context.gameState,
                controlling.getCenterLocation(),
                GridLocationQuerier.createNaturalResourceFilter(context.gameState, type),
                Double.MAX_VALUE,
                context.currentPlayer
        ));
    }

    protected boolean retrieveCollectedResources(ActionRequester ar, final EntityId constructor, final ResourceType resource, int amountToRetreive) {
        EntityQueryFilter filter = entity -> {
            // Should we make him cut trees?
            EntitySpec type = context.gameState.typeManager.get(entity);
            if (type == null || !type.containsClass("storage") || type.containsClass("construction-zone")) return false;
//            Player player = c.gameState.playerManager.getOwner(constructor);
//            if (player == null) return false;
//            if (!c.gameState.playerManager.playerOwns(player, entity)) return false;

//            if (!type.containsClass("storage")) return false;
            Load load = context.gameState.carryingManager.get(entity);
            if (load == null) return false;
            Integer quantity = load.quantities.get(resource);
            return quantity != null && quantity > 0;
        };

        GridLocationQuerier.NearestEntityQueryResults queryResults = context.gameState.locationManager.query(
                new GridLocationQuerier.NearestEntityQuery(context.gameState, controlling.getLocation(), filter, Double.MAX_VALUE, context.currentPlayer)
        );
        if (!queryResults.successful()) {
            return false;
        }

        if (Proximity.closeEnoughToInteract(context.gameState, constructor, queryResults.entity)) {
            ar.setUnitActionToCollect(controlling, queryResults.entity, resource, amountToRetreive);
            return true;
        }

        ar.setUnitActionToMove(controlling, queryResults.path);
        return true;
    }

    public static boolean anyAreNull(Object... objects) {
        for (Object o : objects) {
            if (o == null) {
                return true;
            }
        }
        return false;
    }
}
