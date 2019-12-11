package client.ai.ai2;

import common.AiAttemptResult;
import common.event.ActionCompleted;
import common.state.EntityReader;
import common.util.DPoint;

public class Move extends AiTask {

    //    private DPoint currentLocation;
    private DPoint destination;

    public Move(EntityReader controlling, DPoint location) {
        super(controlling);
        destination = location;
    }

    @Override
    public String toString() {
        return "move to " + destination;
    }


    protected AiAttemptResult initialize(AiContext aiContext) {
//        DPoint location = entity.getLocation();
//        if (currentLocation != null && currentLocation.equals(location))
//            return AiAttemptResult.Unsuccessful;
//        currentLocation = location;
//        if (currentLocation.distanceTo(destination) < 1e-2)
//            return AiAttemptResult.Completed;
        AiAttemptResult aiAttemptResult = aiContext.requester.setUnitActionToMove(aiContext.clientGameState.pathFinder, this.entity, destination);
        if (aiAttemptResult.failed())
            return aiAttemptResult;
        return AiAttemptResult.RequestedAction;
    }

    protected AiAttemptResult currentActionCompleted(AiContext aiContext, ActionCompleted.ActionCompletedReason reason) {
        return AiAttemptResult.Completed;
    }
}
