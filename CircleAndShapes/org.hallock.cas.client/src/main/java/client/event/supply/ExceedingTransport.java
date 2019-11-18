package client.event.supply;

import common.state.EntityReader;

public class ExceedingTransport extends Transport {
    public ExceedingTransport(TransportRequest request) {
        super(request);
    }

    @Override
    public EntityReader getDropOffLocation() {
        return null;
    }

    @Override
    public EntityReader getPickupLocation() {
        return getRequester();
    }
}
