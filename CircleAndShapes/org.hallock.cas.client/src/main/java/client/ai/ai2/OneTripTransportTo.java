package client.ai.ai2;

import common.AiAttemptResult;
import common.state.EntityReader;
import common.state.spec.ResourceType;
import common.util.MapUtils;

import java.util.Map;

public class OneTripTransportTo extends OneTripTransport {
    private final EntityReader destination;

    public OneTripTransportTo(EntityReader controlling, EntityReader destination) {
        super(controlling, MapUtils.copy(controlling.getCarrying().quantities), TransportState.Delivering);
        this.destination = destination;
    }
    public OneTripTransportTo(EntityReader controlling, EntityReader destination, Map<ResourceType, Integer> resourcesToTransport) {
        super(controlling, resourcesToTransport, TransportState.PickingUp);
        this.destination = destination;
    }

    protected AiAttemptResult perform(AiContext aiContext) {
        switch (state) {
            case Delivering: return deliverResources(aiContext, destination, resourcesToTransport);
            case PickingUp: return pickupCollectedResources(aiContext, resourcesToTransport);
            default: throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        return "Delivering";
    }
}
