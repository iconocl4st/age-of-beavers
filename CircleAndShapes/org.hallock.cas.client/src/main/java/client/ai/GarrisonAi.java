package client.ai;

import client.app.ClientContext;
import common.Proximity;
import common.state.EntityId;

public class GarrisonAi extends Ai {

    private final EntityId garrisonLocation;

    public GarrisonAi(ClientContext state, EntityId toGarrison, EntityId garrisonLocation)  {
        super(state, toGarrison);
        this.garrisonLocation = garrisonLocation;
    }

    public String toString() {
        return "garrison";
    }

    @Override
    public AiAttemptResult setActions(ActionRequester ar) {
        if (controlling.isHidden()) {
            return AiAttemptResult.Completed;
        }
        if (Proximity.closeEnoughToInteract(context.gameState, controlling.entityId, garrisonLocation)) {
            ar.setUnitActionToEnter(controlling, garrisonLocation);
            return AiAttemptResult.Successful;
        } else {
            return ar.setUnitActionToMove(controlling, garrisonLocation);
        }
    }
}
