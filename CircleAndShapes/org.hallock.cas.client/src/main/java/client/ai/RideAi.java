package client.ai;

import client.state.ClientGameState;
import common.AiAttemptResult;
import common.Proximity;
import common.state.EntityReader;

// todo: dry with garrison
public class RideAi extends Ai {

    private final EntityReader ridden;

    public RideAi(ClientGameState context, EntityReader rider, EntityReader ridden) {
        super(context, rider);
        this.ridden = ridden;
    }

    public String toString() {
        return "ride";
    }

    @Override
    public AiAttemptResult setActions(ActionRequester ar) {
        if (Proximity.closeEnoughToInteract(controlling, ridden)) {
            ar.setUnitActionToMount(controlling, ridden);
            return AiAttemptResult.Successful;
        } else {
            return ar.setUnitActionToMove(controlling, ridden);
        }
    }
}