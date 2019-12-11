package client.ai.ai2;

import common.AiAttemptResult;
import common.action.Action;
import common.state.EntityReader;
import common.state.spec.CreationSpec;
import common.state.sst.sub.Load;

public class Produce extends DefaultAiTask {
    private final CreationSpec spec;

    public Produce(EntityReader entity, CreationSpec spec) {
        super(entity);
        this.spec = spec;
    }

    public CreationSpec getCreating() {
        return spec;
    }


    protected AiAttemptResult resourcesChanged(AiContext aiContext) {
        return requestActions(aiContext);
    }

    protected AiAttemptResult garrisonsChanged(AiContext aiContext) {
        return requestActions(aiContext);
    }

    @Override
    protected AiAttemptResult requestActions(AiContext aiContext) {
        if (entity.getGarrisoned().isEmpty()) {
            return AiAttemptResult.Unsuccessful;
        }
        Action currentAction = entity.getCurrentAction();
        if (currentAction instanceof Action.Create)
            return AiAttemptResult.RequestedAction;

        if (!(currentAction instanceof Action.Idle)) {
            return AiAttemptResult.Unsuccessful;
        }
        Load carrying = entity.getCarrying();
        if (carrying == null)
            return AiAttemptResult.Unsuccessful;

        if (!carrying.canAfford(spec.requiredResources)) {
            return AiAttemptResult.RequestedAction;
        }
        aiContext.requester.setUnitActionToCreate(entity, spec);
        return AiAttemptResult.RequestedAction;
    }

    @Override
    public String toString() {
        return "Continuously create " + spec.createdType.name;
    }
}
