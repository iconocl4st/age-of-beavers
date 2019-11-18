package client.ai;

import client.state.ClientGameState;
import common.Proximity;
import common.state.EntityReader;

public class BeRidden extends Ai {

    private final EntityReader rider;

    public BeRidden(ClientGameState state, EntityReader controlling, EntityReader rider)  {
        super(state, controlling);
        this.rider = rider;
    }

    public String toString() {
        return "be ridden by";
    }

    @Override
    public Ai.AiAttemptResult setActions(ActionRequester ar) {
        if (Proximity.closeEnoughToInteract(controlling, rider)) {
            ar.setUnitActionToMount(rider, controlling);
            return Ai.AiAttemptResult.Successful;
        } else {
            return ar.setUnitActionToMove(controlling, rider);
        }
    }
}
