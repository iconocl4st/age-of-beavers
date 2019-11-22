package client.ai.ai2;

import common.AiAttemptResult;
import common.state.EntityReader;
import common.state.spec.ResourceType;

import java.util.Map;

/*
I guess more like a half a trip...
 */
public class OneTripTransportFrom extends OneTripTransport {
    private final EntityReader destination;

    public OneTripTransportFrom(EntityReader controlling, EntityReader destination, Map<ResourceType, Integer> resourcesToTransport) {
        super(controlling, resourcesToTransport, TransportState.PickingUp);
        this.destination = destination;
    }

    private TransportState nextState() {
        switch (state) {
            case PickingUp:
                return null;
            case Delivering:
            default:
                throw new IllegalStateException();
        }
    }

    protected AiAttemptResult perform(AiContext aiContext) {
        switch (state) {
            case PickingUp: return pickupAllResources(aiContext, destination, resourcesToTransport);
            case Delivering: // return deliverResources(aiContext, resourcesToTransport);
            default: throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        return "Picking up";
    }
}
