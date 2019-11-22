package client.ai.ai2;

import common.AiAttemptResult;
import common.state.EntityReader;

public abstract class WhileWithinProximity extends DefaultAiTask {

    private final OnProximity callback;
    private final EntityReader destination;


    protected WhileWithinProximity(EntityReader entity, EntityReader destination, OnProximity onProximity) {
        super(entity);
        this.callback = onProximity;
        this.destination = destination;
    }

    @Override
    protected AiAttemptResult requestActions(AiContext aiContext) {
        AiAttemptResult result = AiUtils.moveToProximity(aiContext.controlling(entity), destination);
        if (result.didSomething()) return result;
        return callback.onProximity(aiContext);
    }

    public interface OnProximity {
        AiAttemptResult onProximity(AiContext context);
    }

    public static WhileWithinProximity createBeRidden(EntityReader entity, EntityReader rider) {
        return new WhileWithinProximity(entity, rider, c -> {
            c.requester.setUnitActionToMount(rider, entity);
            return AiAttemptResult.Completed;
        }) {
            @Override
            public String toString() {
                return "Entering a " + rider.getType().name;
            }
        };
    }

    public static WhileWithinProximity createGarrison(EntityReader entity, EntityReader toGarrisonIn) {
        return new WhileWithinProximity(entity, toGarrisonIn, c -> {
            c.requester.setUnitActionToEnter(entity, toGarrisonIn);
            return AiAttemptResult.Completed;
        }) {
            @Override
            public String toString() {
                return "Entering a " + toGarrisonIn.getType().name;
            }
        };
    }

    public static WhileWithinProximity createRide(EntityReader entity, EntityReader ridden) {
        return new WhileWithinProximity(entity, ridden, c -> {
            c.requester.setUnitActionToMount(entity, ridden);
            return AiAttemptResult.Completed;
        }) {
            @Override
            public String toString() {
                return "Mounting a " + ridden.getType().name;
            }
        };
    }
}
