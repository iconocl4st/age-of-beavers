package client.ai;

import client.state.ClientGameState;
import common.AiAttemptResult;
import common.state.EntityReader;
import common.util.DPoint;

public class MoveAi extends Ai {
    private DPoint currentLocation;
    private DPoint destination;

    public MoveAi(ClientGameState context, EntityReader controlling, DPoint location) {
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
        if (ar.setUnitActionToMove(controlling, destination).equals(AiAttemptResult.Unsuccessful))
            return AiAttemptResult.Unsuccessful;
        return AiAttemptResult.Successful;
    }
}
