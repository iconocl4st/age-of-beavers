package client.ai.ai2;

import common.AiAttemptResult;
import common.state.EntityReader;
import common.state.spec.EntitySpec;
import common.state.spec.ResourceType;

import java.util.HashSet;
import java.util.Map;

public class Gather extends DefaultAiTask {

    private final EntitySpec naturalResourceSpec;
    private final HashSet<ResourceType> gatheringResources;
    private EntityReader currentTarget;
    private GatherState currentState;

    private enum GatherState {
        Delivering,
        Gathering,
    }

    public Gather(EntityReader gatherer, EntityReader target, EntitySpec resourceType) {
        super(gatherer);
        this.currentTarget = target;
        this.naturalResourceSpec = resourceType;
        this.currentState = GatherState.Delivering;
        gatheringResources = new HashSet<>(naturalResourceSpec.carrying.keySet());
    }

    public EntityReader getCurrentResource() {
        return currentTarget;
    }
    public HashSet<ResourceType> getGatheringResourceTypes() {
        return gatheringResources;
    }

    public String toString() {
        return "Gathering " + naturalResourceSpec.name;
    }

    @Override
    protected AiAttemptResult requestActions(AiContext aiContext) {
        aiContext = aiContext.controlling(entity);
        int count = 0;
        while (true) {
            if (++count > 1000) {
                throw new IllegalStateException();
            }
            switch (currentState) {
                case Delivering: {
                    AiAttemptResult result = OneTripTransport.deliverAllResources(aiContext);
                    if (result.requested()) return result;
                    currentState = GatherState.Gathering;
                } // fall through
                case Gathering: {
                    if (currentTarget == null || currentTarget.noLongerExists() || currentTarget.getCarrying().getWeight() == 0)
                        currentTarget = aiContext.locator.locateNearestNaturalResource(entity, naturalResourceSpec);
                    if (currentTarget == null)
                        return AiAttemptResult.Completed;
                    AiAttemptResult result = OneTripTransport.pickupAllResources(aiContext, currentTarget);
                    if (result.failed()) { // idk about this...
                        currentTarget = null;
                        continue;
                    }
                    if (result.requested()) return result;
                    currentState = GatherState.Delivering;
                }
            }
        }
    }
}
