//package client.ai;
//
//import client.event.AiEventListener;
//import client.state.ClientGameState;
//import common.AiAttemptResult;
//import common.event.AiEvent;
//import common.Proximity;
//import common.event.AiEventType;
//import common.state.EntityId;
//import common.state.EntityReader;
//import common.state.Player;
//import common.state.spec.EntitySpec;
//import common.state.spec.ResourceType;
//import common.state.sst.sub.Load;
//import common.util.query.EntityQueryFilter;
//import common.util.query.GridLocationQuerier;
//import common.util.query.NearestEntityQuery;
//import common.util.query.NearestEntityQueryResults;
//
//import java.util.Collections;
//import java.util.Map;
//import java.util.Set;
//
//public abstract class Ai implements AiEventListener {
//    protected final ClientGameState context;
//    protected final EntityReader controlling;
//
//    protected Ai(ClientGameState context, EntityReader controlling) {
//        this.context = context;
//        this.controlling = controlling;
//    }
//
//    public abstract String toString();
//
//    @Override
//    public void receiveEvent(AiEvent event, ActionRequester ar) {
//        if (!event.type.equals(AiEventType.ActionCompleted) || !event.entity.equals(controlling.entityId))
//            return;
//        switch (setActions(ar)) {
//            case Unsuccessful:
//            case Completed:
//                context.aiManager.removeAi(controlling.entityId);
//                break;
//        }
//    }
//
//    public abstract AiAttemptResult setActions(ActionRequester ar);
//
//    protected AiAttemptResult dropOffOtherResources(ActionRequester ar, ResourceType resourceType) {
//        return dropOffOtherResources(ar, Collections.singleton(resourceType));
//    }
//
//    protected AiAttemptResult dropOffOtherResources(ActionRequester ar, Set<ResourceType> resourceTypes) {
//        Load load = controlling.getCarrying();
//        if (load == null)
//            return AiAttemptResult.Unsuccessful;
//        for (Map.Entry<ResourceType, Integer> entry : load.quantities.entrySet()) {
//            if (resourceTypes.contains(entry.getKey())) {
//                continue;
//            }
//            if (entry.getValue() <= 0) {
//                continue;
//            }
//            return deliverToNearestDropOff(ar, entry.getKey(), controlling.entityId);
//        }
//        return AiAttemptResult.NothingDone;
//    }
//
//    // TODO cleanup, dry
//    protected AiAttemptResult deliverToNearestDropOff(ActionRequester ar, ResourceType resource, EntityId avoid) {
//        NearestEntityQueryResults results = findNearestDropOff(resource, avoid);
//        if (!results.successful()) {
//            System.out.println("No where to leave it.");
//            return AiAttemptResult.Unsuccessful;
//        }
//        if (Proximity.closeEnoughToInteract(controlling, results.getEntity(context.gameState))) {
//            ar.setUnitActionToDeposit(controlling, results.getEntity(context.gameState), resource, Integer.MAX_VALUE);
//        } else {
//            ar.setUnitActionToMove(controlling, results.path);
//        }
//        return AiAttemptResult.RequestedAction;
//    }
//
//    protected NearestEntityQueryResults findNearestDropOff(final ResourceType resource, final EntityId avoid) {
//        EntityQueryFilter filter = entityId -> {
//            if (entityId.equals(avoid)) return false;
//            EntityReader entity = new EntityReader(context.gameState, entityId);
//            if (entity.getType() == null) return false;
//            if (entity.isHidden()) return false;
//            if (!entity.canAccept(resource)) return false;
//            if (!entity.getType().containsClass("storage")) return false;
//            if (!entity.getOwner().equals(context.currentPlayer) && !entity.getOwner().equals(Player.GAIA))
//                return false;
//            return true;
//        };
//
//        // TODO: limit max value?
//        NearestEntityQuery query = new NearestEntityQuery(context.gameState, controlling.getCenterLocation(), filter, Double.MAX_VALUE, context.currentPlayer);
//        return context.gameState.locationManager.query(query);
//    }
//
//    protected NearestEntityQueryResults findNearestResource(String type) {
//        return context.gameState.locationManager.query(new NearestEntityQuery(
//                context.gameState,
//                controlling.getCenterLocation(),
//                GridLocationQuerier.createNonEmptyNaturalResourceFilter(context.gameState, type),
//                Double.MAX_VALUE,
//                context.currentPlayer
//        ));
//    }
//
//    protected NearestEntityQueryResults locateCollectedResources(
//        EntityReader searcher,
//        ResourceType resource
//    ) {
//
//        EntityQueryFilter filter = entityId -> {
//            if (entityId.equals(searcher.entityId))
//                return false;
//            EntityReader entity = new EntityReader(context.gameState, entityId);
//            if (!entity.getOwner().equals(context.currentPlayer) && !entity.getOwner().equals(Player.GAIA))
//                return  false;
//            EntitySpec type = entity.getType();
//            if (type == null || !type.containsClass("storage"))
//                return false;
//            /* if (type.containsClass("construction-zone")) return false; */
//            return !entity.doesNotHave(resource);
//        };
//
//        return context.gameState.locationManager.query(
//                new NearestEntityQuery(context.gameState, controlling.getLocation(), filter, Double.MAX_VALUE, context.currentPlayer)
//        );
//    }
//
//    protected AiAttemptResult retrieveCollectedResources(
//            ActionRequester ar,
//            final EntityReader constructor,
//            final ResourceType resource,
//            int amountToRetreive
//    ) {
//        NearestEntityQueryResults results = locateCollectedResources(constructor, resource);
//        if (!results.successful()) {
//            return AiAttemptResult.Unsuccessful;
//        }
//
//        if (Proximity.closeEnoughToInteract(constructor, results.getEntity(context.gameState))) {
//            ar.setUnitActionToCollect(controlling, results.getEntity(context.gameState), resource, amountToRetreive);
//            return AiAttemptResult.RequestedAction;
//        }
//
//        ar.setUnitActionToMove(controlling, results.path);
//        return AiAttemptResult.RequestedAction;
//    }
//
//    void registerListeners() {
//        context.eventManager.listenForEventsFrom(this, controlling.entityId);
//    }
//    void removeListeners() {
//        context.eventManager.stopListeningTo(this, controlling.entityId);
//    }
//
//    static boolean anyAreNull(Object... objects) {
//        for (Object o : objects) {
//            if (o == null) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//
//
//}
