//package client.ai;
//
//import client.ai.ai2.AiContext;
//import client.state.ClientGameState;
//import common.AiAttemptResult;
//import common.Proximity;
//import common.state.EntityReader;
//import common.state.spec.ConstructionSpec;
//import common.state.spec.ResourceType;
//import common.state.sst.sub.ConstructionZone;
//import common.state.sst.sub.Load;
//import common.util.DPoint;
//import common.util.MapUtils;
//
//import java.util.Map;
//import java.util.Set;
//
//public class ConstructAi extends Ai {
//
//    private EntityReader constructionEntity;
//
//    public ConstructAi(ClientGameState gameState, EntityReader controlling, EntityReader constructionZone) {
//        super(gameState, controlling);
//        this.constructionEntity = constructionZone;
//    }
//
//    public String toString() {
//        return "construct";
//    }
//
//    @Override
//    public AiAttemptResult setActions(ActionRequester ar) {
//        while (true) {
//            if (constructionEntity == null) {
//
//                constructionEntity = query.getEntity(context.gameState);
//            }
//
//            DPoint constructorLocation = controlling.getLocation();
//            DPoint constructionLocation =constructionEntity.getLocation();
//            ConstructionZone constructionZone = constructionEntity.getConstructionZone();
//            ConstructionSpec constructionType = (ConstructionSpec) constructionEntity.getType();
//            Load constructionLoad = constructionEntity.getCarrying();
//            Load constructorLoad = controlling.getCarrying();
//            if (anyAreNull(constructorLocation, constructionLocation, constructionZone, constructionType, constructionLoad, constructorLoad)) {
//                constructionEntity = null;
//                continue;
//            }
//
//            Map<ResourceType, Integer> requiredResources = constructionType.resultingStructure.requiredResources;
//
//            // Get the resources still missing...
//            Map<ResourceType, Integer> missingResources = constructionEntity.getMissingConstructionResources();
//
//            if (missingResources.isEmpty()) {
//                /* If all materials are present, then build. */
//                if (Proximity.closeEnoughToInteract(controlling, constructionEntity)) {
//                    ar.setUnitActionToBuild(controlling, constructionEntity);
//                    return AiAttemptResult.RequestedAction;
//                }
//                return ar.setUnitActionToMove(controlling, constructionEntity);
//            }
//
//
//            // TODO: Could have more of the resource than the building needs.
//            // This would greatly reduce the amount that can be delivered.
//
//            // Drop off all of the other resources.
//            AiAttemptResult deliveryAttempt = dropOffOtherResources(ar, missingResources.keySet());
//            if (!deliveryAttempt.equals(AiAttemptResult.NothingDone))
//                // completed??
//                return deliveryAttempt;
//
//            /* Try to deliverToNearestDropOff what we are already carrying */
//            for (Map.Entry<ResourceType, Integer> entry : constructorLoad.quantities.entrySet()) {
//                Integer amountCarrying = entry.getValue();
//                if (amountCarrying == null || entry.getValue() <= 0) {
//                    continue;
//                }
//                Integer amountMissing = missingResources.get(entry.getKey());
//                if (amountMissing == null) {
//                    // shouldn't happen
//                    continue;
//                }
//                if (Proximity.closeEnoughToInteract(controlling, constructionEntity)) {
//                    ar.setUnitActionToDeposit(controlling, constructionEntity, entry.getKey(), requiredResources.get(entry.getKey()));
//                    return AiAttemptResult.RequestedAction;
//                }
//                return ar.setUnitActionToMove(controlling, constructionEntity);
//            }
//
//            /* Otherwise, collect the required resources from other storage locations*/
//            for (Map.Entry<ResourceType, Integer> entry : missingResources.entrySet()) {
//                switch (retrieveCollectedResources(ar, controlling, entry.getKey(), entry.getValue())) {
//                    case RequestedAction:
//                        return AiAttemptResult.RequestedAction;
//                }
//            }
//
//            // there are missing resources, and we can't find them...
//            return AiAttemptResult.Unsuccessful;
//            // close enough is building is completed....
//            // unless we want to build others nearby...
//        }
//    }
//
//}
