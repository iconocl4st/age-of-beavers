package client.ai.ai2;

import common.AiAttemptResult;
import common.state.EntityReader;

public class DeliverAll extends DefaultAiTask {

    public DeliverAll(EntityReader entity) {
        super(entity);
    }

    @Override
    protected AiAttemptResult requestActions(AiContext aiContext) {
        if (entity.getCarrying().getWeight() == 0) return AiAttemptResult.Completed;
        AiAttemptResult result = OneTripTransport.deliverAllResources(aiContext.controlling(entity));
        if (result.didSomething()) return result;
        return AiAttemptResult.Unsuccessful;
    }

    @Override
    public String toString() {
        return "delivering";
    }
}
