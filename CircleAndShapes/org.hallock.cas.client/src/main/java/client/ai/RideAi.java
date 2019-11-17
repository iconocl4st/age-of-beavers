package client.ai;

import client.app.ClientContext;
import common.Proximity;
import common.state.EntityId;

// todo: dry with garrison
public class RideAi extends Ai {

    private final EntityId ridden;

    public RideAi(ClientContext context, EntityId rider, EntityId ridden) {
        super(context, rider);
        this.ridden = ridden;
    }

    public String toString() {
        return "ride";
    }

    @Override
    public AiAttemptResult setActions(ActionRequester ar) {
        if (Proximity.closeEnoughToInteract(context.gameState, controlling.entityId, ridden)) {
            ar.setUnitActionToMount(controlling, ridden);
            return AiAttemptResult.Successful;
        } else {
            return ar.setUnitActionToMove(controlling, ridden);
        }
    }
}