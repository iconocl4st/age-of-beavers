package client.ai;

import client.app.ClientContext;
import common.Proximity;
import common.state.spec.ResourceType;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.sst.sub.Load;

import java.util.Map;

public class DeliverAi extends Ai {

    private final EntityReader destination;
    private final boolean deliverToOthers;

    public DeliverAi(ClientContext state, EntityId carrier, EntityId destination) {
        this(state, carrier, destination, false);
    }

    public DeliverAi(ClientContext state, EntityId carrier, EntityId destination, boolean deliverToOthers) {
        super(state, carrier);
        this.destination = new EntityReader(state.gameState, destination);
        this.deliverToOthers = deliverToOthers;
    }

    public String toString() {
        return "deliver";
    }

    @Override
    public AiAttemptResult setActions(ActionRequester ar) {
        if (!destination.getType().containsClass("storage")) {
            return AiAttemptResult.Unsuccessful;
        }
        Load load = controlling.getCarrying();
        if (load == null) {
            return AiAttemptResult.Unsuccessful;
        }
        double weight = load.getWeight();
        if (weight <= 0) {
            return AiAttemptResult.Completed;
        }
        for (Map.Entry<ResourceType, Integer> entry : load.quantities.entrySet()) {
            if (entry.getValue() <= 0)
                continue;
            if (!destination.canAccept(entry.getKey()))
                continue;

            if (Proximity.closeEnoughToInteract(context.gameState, controlling.entityId, destination.entityId)) {
                ar.setUnitActionToDeposit(controlling, destination.entityId, entry.getKey(), Integer.MAX_VALUE);
            } else {
                ar.setUnitActionToMove(controlling, destination.entityId);
            }
            return AiAttemptResult.Successful;
        }
//        if (deliverToOthers) {
//            for (Map.Entry<ResourceType, Double> entry : load.quantities.entrySet()) {
//                if (entry.getValue() <= Proximity.ZERO_TOLERANCE)
//                    continue;
//                if (deliverToNearestDropOff(ar, entry.getKey(), isControlling)) {
//                    return AiAttemptResult.Successful;
//                }
//            }
//        }
        return AiAttemptResult.Completed;
    }
}
