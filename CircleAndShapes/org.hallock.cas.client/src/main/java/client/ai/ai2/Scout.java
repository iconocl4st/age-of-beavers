package client.ai.ai2;

import common.AiAttemptResult;
import common.state.EntityReader;

public class Scout extends DefaultAiTask {

    protected Scout(EntityReader entity) {
        super(entity);
    }

    @Override
    protected AiAttemptResult requestActions(AiContext aiContext) {
        return AiAttemptResult.NothingDone;
    }

    @Override
    public String toString() {
        return "Scout";
    }
}
