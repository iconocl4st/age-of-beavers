package client.event.supply;

import common.state.EntityId;
import common.state.EntityReader;
import common.state.spec.ResourceType;

public abstract class Transport {
    protected final TransportRequest request;

    protected Transport(TransportRequest request) {
        this.request = request;
    }

    public final ResourceType getResourceType() {
        return request.resource;
    }

    public abstract EntityReader getDropOffLocation();

    public abstract EntityReader getPickupLocation();

    final EntityReader getRequester() {
        return request.requester;
    }
}
