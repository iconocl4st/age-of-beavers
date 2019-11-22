package client.ai.ai2;

import client.event.supply.Transport;
import common.AiAttemptResult;
import common.event.AiEventType;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.spec.ResourceType;

import java.util.HashSet;

public class TransportAi extends DefaultAiTask {

    enum TransportState {
        PickingUp,
        Delivering,
    }

    private final HashSet<EntityId> currentlyListeningTo = new HashSet<>();
    TransportState state = TransportState.PickingUp;
    Transport transport;

    public TransportAi(EntityReader entity) {
        super(entity);
    }

    public EntityReader getServicer() {
        return entity;
    }

    protected AiAttemptResult resourcesChanged(AiContext aiContext) {
        return requestActions(aiContext);
    }

    protected AiAttemptResult demandsChanged(AiContext aiContext) {
        return requestActions(aiContext);
    }

    @Override
    public String toString() {
        return "transporting";
    }


    @Override
    protected AiAttemptResult requestActions(AiContext c) {
        AiAttemptResult result;
        final AiContext aiContext = c.controlling(entity);

        while (true) {
            if (transport == null) {
                transport = aiContext.clientGameState.supplyAndDemandManager.commitToNextTransportationRequest(
                        this,
                        () -> stopListeningToDemandChanges(aiContext),
                        () -> listenForDemandChanges(aiContext) // needs to happen before the sync is released...
                );
                if (transport == null) {
                    stopListeningToResourceChanges(aiContext);
                    return AiAttemptResult.RequestedAction;
                } else {
                    state = TransportState.PickingUp;
                }
            }

            ResourceType resourceType = transport.getResourceType();
            switch (state) {
                case PickingUp: {
                    EntityReader pickupLocation = transport.getPickupLocation();
                    if (pickupLocation == null) {
                        result = OneTripTransport.pickupCollectedResource(aiContext, transport.getResourceType(), Integer.MAX_VALUE);
                    } else {
                        result = OneTripTransport.pickupAllResources(aiContext, pickupLocation, transport.getResourceType(), Integer.MAX_VALUE);
                    }
                    switch (result) {
                        case NothingDone: // within range of deliver site
                            throw new IllegalStateException();
                        case Completed: // can't pickup anymore
                            state = TransportState.Delivering;
                            continue;
                        case Unsuccessful: // no storage or we cannot move to them
                            if (pickupLocation == null && entity.isCarrying(resourceType)) {
                                state = TransportState.Delivering;
                                continue;
                            }
                            listenForDemandChanges(aiContext);
                            listenForResourceChanges(aiContext);
                            return AiAttemptResult.RequestedAction;
                        case RequestedAction:
                            stopListeningToResourceChanges(aiContext);
                            stopListeningToDemandChanges(aiContext);
                            return result;
                    }
                }
                break;
                case Delivering: {
                    EntityReader dropOffLocation = transport.getDropOffLocation();
                    if (dropOffLocation == null) {
                        result = OneTripTransport.deliverResource(aiContext, transport.getResourceType(), Integer.MAX_VALUE);
                    } else {
                        result = OneTripTransport.deliverResource(aiContext, dropOffLocation, transport.getResourceType(), Integer.MAX_VALUE);
                    }
                    switch (result) {
                        case NothingDone: // within range of deliver site (which goes on to requested action)
                            throw new IllegalStateException();
                        case Completed: // don't have any more or storage site or cannot accept more
                            stopServicing(aiContext);
                            continue;
                        case Unsuccessful: // no storage sites or unable to move to them
                            listenForDemandChanges(aiContext);
                            listenForResourceChanges(aiContext);
                            return AiAttemptResult.RequestedAction;
                        case RequestedAction:
                            stopListeningToResourceChanges(aiContext);
                            stopListeningToDemandChanges(aiContext);
                            return result;
                    }
                }
                break;
                default:
                    throw new IllegalStateException();
            }
        }
    }


    private void listenForDemandChanges(AiContext aiContext) {
        aiContext.clientGameState.eventManager.listenForEvents(aiContext.stack, AiEventType.DemandsChanged);
    }
    private void stopListeningToDemandChanges(AiContext aiContext) {
        aiContext.clientGameState.eventManager.stopListeningTo(aiContext.stack, AiEventType.DemandsChanged);
    }

    private void listenForResourceChanges(AiContext aiContext) {
        synchronized (currentlyListeningTo) {
            for (EntityReader reader : aiContext.locator.getStorageLocations()) {
                currentlyListeningTo.add(reader.entityId);
                aiContext.clientGameState.eventManager.listenForEventsFrom(aiContext.stack, reader.entityId); // AiEventType.ResourceChange);
            }
        }
    }

    private void stopListeningToResourceChanges(AiContext aiContext) {
        synchronized (currentlyListeningTo) {
            for (EntityId entityId : currentlyListeningTo) {
                aiContext.clientGameState.eventManager.stopListeningTo(aiContext.stack, entityId);
            }
            currentlyListeningTo.clear();
        }
    }


    private void stopServicing(AiContext aiContext) {
        if (transport == null) return;
        aiContext.clientGameState.supplyAndDemandManager.stopServicing(this, transport);
        transport = null;
    }

    @Override
    public void addExtraListeners(AiContext aiContext) {}

    @Override
    public void removeExtraListeners(AiContext aiContext) {
        stopServicing(aiContext);
        stopListeningToResourceChanges(aiContext);
        stopListeningToDemandChanges(aiContext);
    }
}
