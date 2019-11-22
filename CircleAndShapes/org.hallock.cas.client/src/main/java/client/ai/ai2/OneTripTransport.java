
package client.ai.ai2;

import common.AiAttemptResult;
import common.state.EntityReader;
import common.state.spec.ResourceType;
import common.util.MapUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class OneTripTransport extends DefaultAiTask {

    final Map<ResourceType, Integer> resourcesToTransport;

    enum TransportState {
        PickingUp,
        Delivering,
    }

    TransportState state;

    OneTripTransport(EntityReader controlling, Map<ResourceType, Integer> resourcesToTransport, TransportState startState) {
        super(controlling);
        this.state = startState;
        this.resourcesToTransport = resourcesToTransport;
    }

    private TransportState nextState() {
        switch (state) {
            case PickingUp:
                return TransportState.Delivering;
            case Delivering:
                return null;
            default:
                throw new IllegalStateException();
        }
    }

    protected abstract AiAttemptResult perform(AiContext aiContext);

    @Override
    protected AiAttemptResult requestActions(AiContext aiContext) {
        while (state != null) {
            if (MapUtils.isEmpty(resourcesToTransport))
                return AiAttemptResult.Completed;

            aiContext = aiContext.controlling(entity);
            AiAttemptResult result = deliverOtherResources(aiContext, resourcesToTransport.keySet());
            if (result.didSomething()) return result;

            result = perform(aiContext);
            switch (result) {
                case Completed:
                case NothingDone:
                    state = nextState();
                    continue;
                case RequestedAction:
                case Unsuccessful:
                    return result;
                default:
                    throw new IllegalStateException();
            }
        }
        return AiAttemptResult.Completed;
    }

    // Intellij is messing with the names here.
    // Have to manually go fix them...
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static AiAttemptResult pickupAllResources(AiContext aiContext, EntityReader collectionResource) {
        return pickupAllResources(aiContext, collectionResource, collectionResource.getCarrying().quantities);
    }

    public static AiAttemptResult deliverAllResources(AiContext aiContext) {
        return deliverOtherResources(aiContext, Collections.emptySet());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static AiAttemptResult pickupAllResources(AiContext aiContext, EntityReader collectionResource, HashMap<ResourceType,Integer> quantities) {
        for (Map.Entry<ResourceType, Integer> entry : quantities.entrySet()) {
            AiAttemptResult result = pickupAllResources(aiContext, collectionResource, entry.getKey(), entry.getValue());
            if (result.requested()) return result;
        }
        return AiAttemptResult.NothingDone;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static AiAttemptResult deliverOtherResources(AiContext aiContext, Set<ResourceType> resources) {
        Map<ResourceType, Integer> carrying = aiContext.controlling.getCarrying().quantities;
        for (Map.Entry<ResourceType, Integer> entry : carrying.entrySet()) {
            if (entry.getValue() == null || entry.getValue() == 0) continue;
            if (resources.contains(entry.getKey())) continue;
            AiAttemptResult result = deliverResource(aiContext, entry.getKey(), entry.getValue());
            if (result.requested()) return result;
        }
        return AiAttemptResult.NothingDone;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static AiAttemptResult pickupCollectedResources(AiContext aiContext, Map<ResourceType, Integer> resourceQuantities) {
        for (Map.Entry<ResourceType, Integer> entry : resourceQuantities.entrySet()) {
            AiAttemptResult result = pickupCollectedResource(aiContext, entry.getKey(), entry.getValue());
            if (result.requested()) return result;
        }
        return AiAttemptResult.NothingDone;
    }
    public AiAttemptResult deliverResources(AiContext aiContext, Map<ResourceType,Integer> resourcesToTransport) {
        for (Map.Entry<ResourceType, Integer> entry : resourcesToTransport.entrySet()) {
            AiAttemptResult result = deliverResource(aiContext, entry.getKey(), entry.getValue());
            if (result.requested()) return result;
        }
        return AiAttemptResult.NothingDone;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static AiAttemptResult deliverResources(AiContext aiContext, EntityReader destination, Map<ResourceType, Integer> entries) {
        for (Map.Entry<ResourceType, Integer> entry : entries.entrySet()) {
            AiAttemptResult result = deliverResource(aiContext, destination, entry.getKey(), entry.getValue());
            if (result.requested()) return result;
        }
        return AiAttemptResult.NothingDone;
    }
    public AiAttemptResult pickupAllResources(AiContext aiContext, EntityReader destination, Map<ResourceType, Integer> resourcesToTransport) {
        for (Map.Entry<ResourceType, Integer> entry : resourcesToTransport.entrySet()) {
            AiAttemptResult result = pickupAllResources(aiContext, destination, entry.getKey(), entry.getValue());
            if (result.requested()) return result;
        }
        return AiAttemptResult.NothingDone;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static AiAttemptResult deliverResource(AiContext aiContext, ResourceType key, Integer value) {
        EntityReader destination = aiContext.locator.locateNearestDropOffSite(aiContext.controlling, key, aiContext.controlling);
        if (destination == null) return AiAttemptResult.Unsuccessful;
        return deliverResource(aiContext, destination, key, value);
    }
    public static AiAttemptResult pickupCollectedResource(AiContext aiContext, ResourceType key, Integer value) {
        EntityReader destination = aiContext.locator.locateNearestPickupSite(aiContext.controlling, key, aiContext.controlling);
        if (destination == null) return AiAttemptResult.Unsuccessful;
        return pickupAllResources(aiContext, destination, key, value);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static AiAttemptResult deliverResource(AiContext aiContext, EntityReader destination, ResourceType key, Integer value) {
        if (aiContext.controlling.doesNotHave(key) || !destination.canAccept(key))
            return AiAttemptResult.Completed;
        AiAttemptResult result = AiUtils.moveToProximity(aiContext, destination);
        if (result.didSomething()) return result;
        aiContext.requester.setUnitActionToDeposit(aiContext.controlling, destination, key, value);
        return AiAttemptResult.RequestedAction;
    }
    public static AiAttemptResult pickupAllResources(AiContext aiContext, EntityReader destination, ResourceType key, Integer value) {
        if (destination.doesNotHave(key) || !aiContext.controlling.canAccept(key))
            return AiAttemptResult.Completed;
        AiAttemptResult result = AiUtils.moveToProximity(aiContext, destination);
        if (result.didSomething()) return result;
        aiContext.requester.setUnitActionToCollect(aiContext.controlling, destination, key, value);
        return AiAttemptResult.RequestedAction;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public enum TransportResult {
        RequestedAction,
        ReceiveCannotAccept,
        NoStorageFound,
        NoPathFound,
        NothingToTransport,
    }
}
