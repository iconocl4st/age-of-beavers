package client.ai;

import client.app.ClientContext;
import common.AiEvent;
import common.action.Action;
import common.state.spec.CreationSpec;
import common.state.EntityId;
import common.state.sst.sub.Load;


public class CreateAi extends Ai {

    private final CreationSpec spec;

    public CreateAi(ClientContext context, EntityId controlling, CreationSpec spec) {
        super(context, controlling);
        this.spec = spec;
    }
    @Override
    public String toString() {
        return "Continuously create " + spec.createdType.name;
    }

    @Override
    public void receiveEvent(AiEvent event, ActionRequester ar) {
        super.receiveEvent(event, ar);
        if (!event.entity.equals(controlling.entityId))
            return;
        if (!event.type.equals(AiEvent.EventType.ResourceChange) && !event.type.equals(AiEvent.EventType.GarrisonChange)) {
            return;

        }
        switch (setActions(ar)) {
            case Unsuccessful:
            case Completed:
                context.aiManager.removeAi(controlling.entityId);
                break;
        }
    }

    @Override
    public AiAttemptResult setActions(ActionRequester ar) {
        if (controlling.getGarrisoned().isEmpty()) {
            return AiAttemptResult.Unsuccessful;
        }
        Action currentAction = controlling.getCurrentAction();
        if (currentAction instanceof Action.Create)
            return AiAttemptResult.Successful;
        if (!(currentAction  instanceof Action.Idle)) {
            return AiAttemptResult.Unsuccessful;
        }
        Load carrying = controlling.getCarrying();
        if (carrying == null)
            return AiAttemptResult.Unsuccessful;

        if (!carrying.canAfford(spec.createdType.requiredResources)) {
            return AiAttemptResult.Successful;
        }
        ar.setUnitActionToCreate(controlling, spec);
        return AiAttemptResult.Successful;
    }
}
