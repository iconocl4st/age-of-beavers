package client.ai.ai2;

import common.AiAttemptResult;
import common.state.EntityReader;
import common.state.spec.ResourceType;

import java.util.Map;

public class OneTripTransportFromTo extends OneTripTransport {

    private final EntityReader dropOffLocation;
    private final EntityReader pickupLocation;

    public OneTripTransportFromTo(
            EntityReader controlling,
            EntityReader pickupLocation,
            EntityReader dropOffLocation,
            Map<ResourceType, Integer> resourcesToTransport
    ) {
        super(controlling, resourcesToTransport, TransportState.PickingUp);
        this.pickupLocation = pickupLocation;
        this.dropOffLocation = dropOffLocation;
    }

    @Override
    protected AiAttemptResult perform(AiContext aiContext) {
        switch (state) {
            case Delivering: return deliverResources(aiContext, dropOffLocation, resourcesToTransport);
            case PickingUp: return pickupAllResources(aiContext, pickupLocation, resourcesToTransport);
            default: throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        return "Transporting from to";
    }
}
