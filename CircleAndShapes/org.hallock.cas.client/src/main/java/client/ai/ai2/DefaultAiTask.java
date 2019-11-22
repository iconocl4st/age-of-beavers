package client.ai.ai2;

import common.AiAttemptResult;
import common.event.ActionCompleted;
import common.state.EntityReader;

public abstract class DefaultAiTask extends AiTask {
    protected DefaultAiTask(EntityReader entity) {
        super(entity);
    }

    protected abstract AiAttemptResult requestActions(AiContext aiContext);

    protected AiAttemptResult initialize(AiContext aiContext) {
        return requestActions(aiContext);
    }

    protected AiAttemptResult currentActionCompleted(AiContext aiContext, ActionCompleted.ActionCompletedReason reason) {
        return requestActions(aiContext);
    }

//    protected AiAttemptResult resourcesChanged(AiContext aiContext) {
//        return requestActions(aiContext);
//    }
//
//    protected AiAttemptResult garrisonsChanged(AiContext aiContext) {
//        return requestActions(aiContext);
//    }
//
//    protected AiAttemptResult targetKilled(AiContext aiContext, EntityReader target, Set<EntityReader> readers) {
//        return requestActions(aiContext);
//    }
//
//    protected AiAttemptResult targetWithinRange(AiContext aiContext, EntityReader target) {
//        return requestActions(aiContext);
//    }
}
