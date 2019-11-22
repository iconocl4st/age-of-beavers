package client.ai.ai2;

import common.AiAttemptResult;
import common.state.EntityReader;
import common.state.spec.CarrySpec;
import common.state.spec.EntitySpec;
import common.state.spec.ResourceType;

import java.util.HashSet;

public class Gather extends DefaultAiTask {

    private final EntitySpec naturalResourceSpec;
    private final HashSet<ResourceType> gatheringResources = new HashSet<>();
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

        for (CarrySpec spec : naturalResourceSpec.carrying) {
            gatheringResources.add(spec.type);
        }
    }

    public EntityReader getCurrentResource() {
        return currentTarget;
    }

    public String toString() {
        return "Gathering " + naturalResourceSpec.name;
    }

    @Override
    protected AiAttemptResult requestActions(AiContext aiContext) {
        aiContext = aiContext.controlling(entity);
        while (true) {
            switch (currentState) {
                case Delivering: {
                    AiAttemptResult result = OneTripTransport.deliverAllResources(aiContext);
                    if (result.requested()) return result;
                    currentState = GatherState.Gathering;
                } // fall through
                case Gathering: {
                    if (currentTarget == null || currentTarget.noLongerExists())
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
