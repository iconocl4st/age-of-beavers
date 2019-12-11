//package client.ai;
//
//import client.state.ClientGameState;
//import common.AiAttemptResult;
//import common.event.AiEvent;
//import common.Proximity;
//import common.event.AiEventType;
//import common.event.TargetKilled;
//import common.state.EntityId;
//import common.state.EntityReader;
//import common.state.spec.EntitySpec;
//import common.state.spec.ResourceType;
//import common.state.spec.attack.Weapon;
//import common.util.DPoint;
//import common.util.query.NearestEntityQuery;
//import common.util.query.NearestEntityQueryResults;
//
//import java.util.*;
//
//public class HuntAi extends Ai {
//
//    private static final double HUNTER_RANGE = 5.0;
//
//    private final EntitySpec preyType;
//
//    private EntityReader currentPrey;
//    private EntityReader currentCarcass;
//
//
//
//    public HuntAi(ClientGameState state, EntityReader hunter, EntityReader prey, EntitySpec preyType) {
//        super(state, hunter);
//        this.preyType = preyType;
//        if (!preyType.containsClass("prey"))
//            throw new IllegalStateException("Cannot hunt this: " +  preyType.name);
//        setCurrentPrey(prey);
//    }
//
//    @Override
//    public synchronized void receiveEvent(AiEvent event, ActionRequester ar) {
//        super.receiveEvent(event, ar);
//
//        if (!event.type.equals(AiEventType.TargetKilled))
//            return;
//        if (currentPrey == null)
//            return;
//        if (!event.entity.equals(currentPrey.entityId))
//            return;
//        setCurrentPrey(null);
//        List<EntityId> droppedUnits = ((TargetKilled) event).droppedUnits;
//        if (droppedUnits.isEmpty()) {
//            currentCarcass = null;
//        } else {
//            // TODO: what if they drop multiple items...
//            currentCarcass = new EntityReader(context.gameState, droppedUnits.get(0));
//        }
//
//        // TODO: what if this is false...
//        setActions(ar);
//    }
//
//    private void setCurrentPrey(EntityReader nextCurrentPrey) {
//        if (currentPrey != null) {
//            context.eventManager.stopListeningTo(this, currentPrey.entityId);
//        }
//        currentPrey = nextCurrentPrey;
//        if (currentPrey != null) {
//            context.eventManager.listenForEventsFrom(this, currentPrey.entityId);
//        }
//    }
//
//    private EntitySpec getCollectingResourceType() {
//        // And when there are multiple?!?!
//        for (EntitySpec toDrop : preyType.dropOnDeath) {
//            if (!toDrop.containsClass("natural-resource")) {
//                continue;
//            }
//            return toDrop;
//        }
//        throw new IllegalStateException("Should be able to pick something up.");
//    }
//
//    public Weapon getWeapon() {
//    }
//
//    @Override
//    public AiAttemptResult setActions(ActionRequester ar) {
//        DPoint hunterLocation = controlling.getLocation();
//        EntitySpec hunterType = controlling.getType();
//        if (anyAreNull(hunterType, hunterLocation)) {
//            return AiAttemptResult.Unsuccessful;
//        }
//
//        Weapon weaponOfChoice = getWeapon();
//        if (weaponOfChoice == null) {
//            return AiAttemptResult.Unsuccessful;
//        }
//        ResourceType collectingResource = getCollectingResourceType().carrying.iterator().next().type;
//
////        Set<ResourceType> alwaysKeep = weaponOfChoice.weaponType.getAmunitionResources();
//        Set<ResourceType> notOtherResources = weaponOfChoice.weaponType.getAmunitionResources();
//        notOtherResources.add(collectingResource);
//
//        AiAttemptResult deliveryAttempt = dropOffOtherResources(ar, notOtherResources);
//        if (!deliveryAttempt.equals(AiAttemptResult.NothingDone)) {
//            return deliveryAttempt;
//        }
//
//        while (true) {
//            if (!controlling.canAccept(collectingResource)) {
//                return deliverToNearestDropOff(ar, collectingResource, controlling.entityId);
//            }
//            if (currentPrey != null) {
//                DPoint targetLocation = currentPrey.getLocation();
//                if (targetLocation == null) {
//                    setCurrentPrey(null);
//                    continue;
//                }
//                if (hunterLocation.distanceTo(targetLocation) < weaponOfChoice.weaponType.rangeCanStartAttacking) {
//                    ar.setUnitActionToAttack(controlling, currentPrey, weaponOfChoice);
//                    return AiAttemptResult.RequestedAction;
//                } else {
//                    return ar.setUnitActionToMove(controlling, currentPrey);
//                }
//            }
//            if (currentCarcass != null) {
//                DPoint targetLocation = currentCarcass.getLocation();
//                if (targetLocation == null) {
//                    currentCarcass = null;
//                    continue;
//                }
//                ResourceType resourceType = currentCarcass.getCarrying().getNonzeroResource();
//                if (resourceType == null) {
//                    currentCarcass = null;
//                    continue;
//                }
//                if (Proximity.closeEnoughToInteract(controlling, currentCarcass)) {
//                    ar.setUnitActionToCollect(controlling, currentCarcass, resourceType);
//                    return AiAttemptResult.RequestedAction;
//                } else {
//                    return ar.setUnitActionToMove(controlling, currentCarcass);
//                }
//            }
//
//            NearestEntityQueryResults results = context.gameState.locationManager.query(
//                    new NearestEntityQuery(
//                            context.gameState,
//                            controlling.getLocation(),
//                            entity -> {
//                                EntitySpec type = context.gameState.typeManager.get(entity);
//                                return type != null && type.name.equals(preyType.name);
//                            },
//                            Double.MAX_VALUE,
//                            context.currentPlayer
//                    )
//            );
//            if (!results.successful()) {
//                return AiAttemptResult.Completed;
//            }
//
//            setCurrentPrey(results.getEntity(context.gameState));
//            ar.setUnitActionToMove(controlling, results.points);
//            return AiAttemptResult.RequestedAction;
//        }
//    }
//}
