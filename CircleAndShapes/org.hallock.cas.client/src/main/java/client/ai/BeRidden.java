package client.ai;

import client.app.ClientContext;
import common.Proximity;
import common.state.EntityId;
import common.state.EntityReader;

public class BeRidden extends Ai {

    private final EntityReader rider;

    public BeRidden(ClientContext state, EntityId controlling, EntityId riderLocation)  {
        super(state, controlling);
        this.rider = new EntityReader(state.gameState, riderLocation);
    }

    public String toString() {
        return "be ridden by";
    }

    @Override
    public Ai.AiAttemptResult setActions(ActionRequester ar) {
        if (Proximity.closeEnoughToInteract(context.gameState, controlling.entityId, rider.entityId)) {
            ar.setUnitActionToMount(rider, controlling.entityId);
            return Ai.AiAttemptResult.Successful;
        } else {
            return ar.setUnitActionToMove(controlling, rider.entityId);
        }
    }
}
