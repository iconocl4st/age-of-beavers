package client.ai.ai2;

import common.AiAttemptResult;
import common.event.*;
import common.state.EntityId;
import common.state.EntityReader;
import common.util.DPoint;

import java.util.Set;

public abstract class AiEventAdapter {
    AiAttemptResult receiveEvent(AiContext aiContext, AiEvent event) {
        switch (event.type) {
            case DemandsChanged:
                DemandsChanged changed = (DemandsChanged) event;
                return demandsChanged(aiContext);
            case ResourceChange:
                ResourcesChanged resourcesChanged = (ResourcesChanged) event;
                return resourcesChanged(aiContext);
            case BuildingPlacementChanged:
                BuildingPlacementChanged placementChanged = (BuildingPlacementChanged) event;
                return AiAttemptResult.NothingDone;
            case ActionCompleted:
                ActionCompleted completion = (ActionCompleted) event;
                return currentActionCompleted(aiContext, completion.reason);
            case GarrisonChange:
                GarrisonedChanged change = (GarrisonedChanged) event;
                return garrisonsChanged(aiContext);
            case TargetKilled:
                TargetKilled killed = (TargetKilled) event;
                return targetKilled(aiContext, new EntityReader(aiContext.gameState, killed.entity), EntityId.getReaders(aiContext.gameState, killed.droppedUnits));
            case TargetWithinRange:
                TargetWithinRange rangeEvent = (TargetWithinRange) event;
                return targetWithinRange(aiContext, rangeEvent.target);
            case Initialize:
                InitializeAi initialize = (InitializeAi) event;
                return initialize(aiContext);
            case UnitChangedDirection:
                UnitChangedDirection directionChange = (UnitChangedDirection) event;
                return unitChangedDirection(
                        aiContext,
                        new EntityReader(aiContext.gameState, directionChange.entity),
                        directionChange.currentLocation,
                        directionChange.endLocation,
                        directionChange.speed
                );
            default:
                throw new RuntimeException("Unhandled event type");
        }
    }

    protected AiAttemptResult demandsChanged(AiContext aiContext) { return AiAttemptResult.NothingDone; }
    protected AiAttemptResult resourcesChanged(AiContext aiContext) { return AiAttemptResult.NothingDone; }
    protected AiAttemptResult garrisonsChanged(AiContext aiContext) { return AiAttemptResult.NothingDone; }
    protected AiAttemptResult initialize(AiContext aiContext) { return AiAttemptResult.NothingDone; }
    protected AiAttemptResult currentActionCompleted(AiContext aiContext, ActionCompleted.ActionCompletedReason reason) {return AiAttemptResult.NothingDone; }
    protected AiAttemptResult targetKilled(AiContext context, EntityReader target, Set<EntityReader> readers) {return AiAttemptResult.NothingDone; }
    protected AiAttemptResult targetWithinRange(AiContext context, EntityReader target) {return AiAttemptResult.NothingDone; }
    protected AiAttemptResult unitChangedDirection(AiContext aiContext, EntityReader entity, DPoint currentLocation, DPoint endLocation, double speed) { return AiAttemptResult.NothingDone; }
}
