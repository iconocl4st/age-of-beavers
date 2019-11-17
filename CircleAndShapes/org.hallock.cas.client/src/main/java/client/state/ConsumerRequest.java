package client.state;

import common.state.spec.ResourceType;
import common.state.EntityId;

public class ConsumerRequest {
    public final ResourceType resource;
    public final int desiredAmount;
    public final double timeRequested;
    public final int priority;
    public final EntityId requester;


    public ConsumerRequest(EntityId requester, ResourceType resource, int desiredAmount, int priority, double timeRequested) {
        this.resource = resource;
        this.desiredAmount = desiredAmount;
        this.timeRequested = timeRequested;
        this.priority = priority;
        this.requester = requester;
    }
}
