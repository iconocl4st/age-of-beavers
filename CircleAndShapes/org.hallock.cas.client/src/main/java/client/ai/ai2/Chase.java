package client.ai.ai2;

import common.AiAttemptResult;
import common.Proximity;
import common.action.Action;
import common.algo.Ballistics;
import common.event.TargetWithinRange;
import common.msg.Message;
import common.state.EntityReader;
import common.state.spec.EntitySpec;
import common.util.DPoint;

import java.util.Set;

public abstract class Chase extends DefaultAiTask {

    protected final EntityReader target;
    protected final double distance;
    // last known location....

    protected Chase(EntityReader entity, EntityReader target, double distance) {
        super(entity);
        this.target = target;
        this.distance = distance;
    }

    protected void addExtraListeners(AiContext aiContext) {
        aiContext.clientGameState.eventManager.listenForEventsFrom(aiContext.stack, target.entityId);
        aiContext.requester.getWriter().send(new Message.ListenForTargetInRange(new TargetWithinRange(entity.entityId, target.entityId, distance), true));
//        aiContext.clientGameState.eventManager.listenForEventsFrom(aiContext.stack, entity);
    }

    protected void removeExtraListeners(AiContext aiContext) {
        aiContext.clientGameState.eventManager.stopListeningTo(aiContext.stack, target.entityId);
        aiContext.requester.getWriter().send(new Message.ListenForTargetInRange(new TargetWithinRange(entity.entityId, target.entityId, distance), false));
    }

    protected abstract boolean targetIsWithinRange();

    protected AiAttemptResult targetKilled(AiContext context, EntityReader target, Set<EntityReader> readers) {
        return AiAttemptResult.Unsuccessful;
    }

    protected AiAttemptResult targetWithinRange(AiContext context, EntityReader target) {
        if (targetIsWithinRange()) {
            return AiAttemptResult.Completed;
        }
        return AiAttemptResult.RequestedAction;
    }

    @Override
    protected AiAttemptResult unitChangedDirection(AiContext aiContext, EntityReader targetId, DPoint currentLocation, DPoint endLocation, double speed) {
        if (!targetId.equals(target)) return AiAttemptResult.NothingDone;
        if (targetIsWithinRange()) return AiAttemptResult.Completed;
        // maybe don't need this method...
        return aiContext.requester.setUnitActionToMove(
                aiContext.clientGameState.pathFinder,
                entity,
                getInterception(entity.getLocation(),
                    entity.getMovementSpeed(),
                    currentLocation,
                    endLocation,
                    speed
                )
        );
    }

    @Override
    protected AiAttemptResult requestActions(AiContext aiContext) {
        if (targetIsWithinRange()) return AiAttemptResult.Completed;
        Object sync = target.getSync();
        if (sync == null) return AiAttemptResult.Unsuccessful;
        synchronized (sync) {
            if (target.noLongerExists()) return AiAttemptResult.Unsuccessful;
            return aiContext.requester.setUnitActionToMove(aiContext.clientGameState.pathFinder, entity, getInterception());
        }
    }

    private DPoint getInterception() {
        Action currentAction = target.getCurrentAction();
        if (!(currentAction instanceof Action.MoveSeq))
            return target.getLocation();
        Action.MoveSeq move = (Action.MoveSeq) currentAction;
        return getInterception(
                entity.getLocation(),
                entity.getMovementSpeed(),
                target.getLocation(),
                (DPoint) move.path.points.get(move.progress),
                target.getMovementSpeed()
        );
    }

    private DPoint getInterception(
            DPoint chaserLocation,
            double chaserSpeed,
            DPoint targetLocationBegin,
            DPoint targetLocationEnd,
            double targetSpeed
    ) {
        if (targetSpeed == 0.0) {
            return targetLocationEnd;
        }
        double dx = targetLocationEnd.x - targetLocationBegin.x;
        double dy = targetLocationEnd.y - targetLocationBegin.y;
        double n = Math.sqrt(dx * dx + dy * dy);
        if (n < 1e-4) {
            return targetLocationEnd;
        }
        Ballistics.Solution intersection = Ballistics.getIntersections(
                chaserLocation.x,
                chaserLocation.y,
                chaserSpeed,
                targetLocationBegin.x,
                targetLocationBegin.y,
                targetSpeed,
                dx / n,
                dy / n
        ).minimumTimeSolution();
        if (intersection == null) {
            return targetLocationEnd;
        }
        return new DPoint(intersection.ix, intersection.iy);
    }


    public static final class ProximityChase extends Chase {
        public ProximityChase(EntityReader entity, EntityReader target) {
            super(entity, target, Proximity.INTERACTION_DISTANCE);
        }

        @Override
        protected boolean targetIsWithinRange() {
            return Proximity.closeEnoughToInteract(entity, target);
        }

        @Override
        public String toString() {
            return "Chasing a " + target.getType().name;
        }
    }

    public static final class RangeChase extends Chase {
        public RangeChase(EntityReader entity, EntityReader target, double distance) {
            super(entity, target, distance);
        }

        @Override
        protected boolean targetIsWithinRange() {
            return entity.getLocation().distanceTo(target.getLocation()) < distance;
        }


        @Override
        public String toString() {
            EntitySpec type = target.getType();
            if (type == null) return "Moving nowhere";
            return "Moving towards a " + type.name;
        }
    }
}
