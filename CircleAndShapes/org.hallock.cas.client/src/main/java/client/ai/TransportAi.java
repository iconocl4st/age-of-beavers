package client.ai;

import client.event.supply.Transport;
import client.state.ClientGameState;
import common.AiAttemptResult;
import common.AiEvent;
import common.state.EntityReader;
import common.state.spec.ResourceType;
import common.util.query.NearestEntityQueryResults;

public class TransportAi extends Ai {

    TransportState state = TransportState.PickingUp;
    Transport transport;
    boolean nowhere;

    public TransportAi(ClientGameState context, EntityReader controlling) {
        super(context, controlling);
    }

    public EntityReader getServicer() {
        return controlling;
    }

    enum TransportState {
        PickingUp,
        Delivering,
    }


    @Override
    public synchronized void receiveEvent(AiEvent event, ActionRequester ar) {
        super.receiveEvent(event, ar);
        if (!event.type.equals(AiEvent.EventType.DemandsChanged) && !event.type.equals(AiEvent.EventType.ResourceChange))
            return;
        if (event.type.equals(AiEvent.EventType.DemandsChanged) && transport != null)
            return;
        if (event.type.equals(AiEvent.EventType.ResourceChange) && !nowhere)
            return;
        setActions(ar);
    }

    @Override
    public String toString() {
        return "transporting";
    }

    @Override
    public synchronized AiAttemptResult setActions(ActionRequester ar) {
        while (true) {
            if (transport == null) {
                transport = context.supplyAndDemandManager.commitToNextTransportationRequest(this);
                if (transport == null) {
                    return AiAttemptResult.Successful;
                }
                state = TransportState.PickingUp;
            }

            ResourceType resourceType = transport.getResourceType();
            if (state.equals(TransportState.PickingUp)) {
                //
                // Should check whether we need to drop off current resources of the same type.
                //
                if (!controlling.canAccept(resourceType)) {
                    state = TransportState.Delivering;
                    continue;
                }
                EntityReader pickupLocation = transport.getPickupLocation();
                AiAttemptResult result;
                if (pickupLocation == null) {
                    NearestEntityQueryResults results = locateCollectedResources(controlling, resourceType);
                    if (!results.successful()) {
                        if (!controlling.doesNotHave(resourceType)) {
                            state = TransportState.Delivering;
                            continue;
                        }
                        nowhere = true;
                        return AiAttemptResult.Successful;
                    }
                    pickupLocation = results.getEntity(context.gameState);
                }
                nowhere = false;
                result = new PickUpAi(context, controlling, pickupLocation, resourceType).setActions(ar);
                switch (result) {
                    case Completed:
                        state = TransportState.Delivering;
                        continue;
                    case Successful:
                        return result;
                    case Unsuccessful:
                        stopServicing();
                        continue;
                    case NothingDone:
                    default:
                        throw new RuntimeException("Uh oh...");
                }
            } else if (state.equals(TransportState.Delivering)) {
                EntityReader dropOffLocation = transport.getDropOffLocation();
                AiAttemptResult result;
                if (dropOffLocation == null) {
                    NearestEntityQueryResults nearestDropOff = findNearestDropOff(resourceType, controlling.entityId);
                    if (!nearestDropOff.successful()) {
                        nowhere = true;
                        return AiAttemptResult.Successful;
                    }
                    dropOffLocation = nearestDropOff.getEntity(context.gameState);
                }
                nowhere = false;
                result = new DeliverAi(context, controlling, dropOffLocation).setActions(ar);
                switch (result) {
                    case Unsuccessful:
                    case Completed:
                        stopServicing();
                        continue;
                    case Successful:
                        return result;
                    default:
                        throw new RuntimeException("uh oh");
                }
            } else {
                throw new RuntimeException("Unknown state " + state);
            }
        }
    }

    private void stopServicing() {
        if (transport == null) return;
        context.supplyAndDemandManager.stopServicing(this, transport);
        transport = null;
    }

    void registerListeners() {
        super.registerListeners();
        context.eventManager.listenForEvents(this, AiEvent.EventType.DemandsChanged);
        context.eventManager.listenForEvents(this, AiEvent.EventType.ResourceChange);
    }

    void removeListeners() {
        super.removeListeners();
        stopServicing();
        context.eventManager.stopListeningTo(this, AiEvent.EventType.DemandsChanged);
        context.eventManager.stopListeningTo(this, AiEvent.EventType.ResourceChange);
    }
}
