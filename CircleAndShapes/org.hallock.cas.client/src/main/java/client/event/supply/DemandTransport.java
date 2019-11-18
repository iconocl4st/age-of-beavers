package client.event.supply;

import common.state.EntityReader;

public class DemandTransport extends Transport {

    DemandTransport(TransportRequest request) {
        super(request);
    }

    @Override
    public EntityReader getDropOffLocation() {
        return getRequester();
    }

    @Override
    public EntityReader getPickupLocation() {
        return null;
    }
}
