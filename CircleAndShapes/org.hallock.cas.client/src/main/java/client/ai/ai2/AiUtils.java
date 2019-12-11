package client.ai.ai2;

import common.AiAttemptResult;
import common.Proximity;
import common.state.EntityReader;
import common.util.DPoint;

public class AiUtils {
    public static AiAttemptResult moveToProximity(AiContext context, EntityReader destination) {
        if (Proximity.closeEnoughToInteract(context.controlling, destination)) {
            return AiAttemptResult.NothingDone;
        }
        return context.requester.setUnitActionToMove(context.clientGameState.pathFinder, context.controlling, destination);
    }
    public static AiAttemptResult moveToRange(AiContext context, EntityReader destination, double radius) {
        DPoint loc1 = context.controlling.getCenterLocation();
        DPoint loc2 = destination.getCenterLocation();
        if (loc1 == null || loc2 == null)
            return AiAttemptResult.Unsuccessful;

        if (loc1.distanceTo(loc2) < radius) {
            return AiAttemptResult.NothingDone;
        }

        return context.requester.setUnitActionToMove(context.clientGameState.pathFinder, context.controlling, destination);
    }
}
