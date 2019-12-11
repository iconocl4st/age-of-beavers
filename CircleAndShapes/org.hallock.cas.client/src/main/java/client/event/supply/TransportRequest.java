package client.event.supply;

import client.ai.ai2.TransportAi;
import common.state.EntityReader;
import common.state.spec.ResourceType;

public class TransportRequest implements Comparable<TransportRequest> {
    public final ResourceType resource;
    public final double timeRequested;
    public final int priority;
    public final EntityReader requester;
    public TransportAi servicer;

    public TransportRequest(EntityReader requester, ResourceType resource, int priority, double timeRequested) {
        this.resource = resource;
        this.timeRequested = timeRequested;
        this.priority = priority;
        this.requester = requester;
        this.servicer = null;
    }

    @Override
    public int compareTo(TransportRequest consumerRequest) {
        int cmp = Integer.compare(priority, consumerRequest.priority);
        if (cmp != 0) return -cmp;
        cmp = Double.compare(timeRequested, consumerRequest.timeRequested);
        if (cmp != 0) return cmp;
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TransportRequest)) {
            return false;
        }
        TransportRequest r = (TransportRequest) o;
        if (compareTo(r) != 0)
            return false;
        return resource.equals(r.resource) && requester.equals(r.requester);
    }
}
