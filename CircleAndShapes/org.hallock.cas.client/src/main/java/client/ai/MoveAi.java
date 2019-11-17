package client.ai;

import client.app.ClientContext;
import common.state.EntityId;
import common.util.DPoint;

public class MoveAi extends Ai {
    private DPoint currentLocation;
    private DPoint destination;

    public MoveAi(ClientContext context, EntityId controlling, DPoint location) {
        super(context, controlling);
        destination = location;
    }

    @Override
    public String toString() {
        return "move to " + destination;
    }

    @Override
    public AiAttemptResult setActions(ActionRequester ar) {
        DPoint location = context.gameState.locationManager.getLocation(controlling.entityId);
        if (currentLocation != null && currentLocation.equals(location))
            return AiAttemptResult.Unsuccessful;
        currentLocation = location;
        if (!ar.setUnitActionToMove(controlling, destination))
            return AiAttemptResult.Unsuccessful;
        return AiAttemptResult.Successful;
    }
}
