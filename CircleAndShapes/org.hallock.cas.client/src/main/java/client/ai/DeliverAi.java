package client.ai;

import client.state.ClientGameState;
import common.Proximity;
import common.state.EntityReader;
import common.state.spec.ResourceType;
import common.state.sst.sub.Load;

import java.util.Map;

public class DeliverAi extends Ai {

    private final EntityReader destination;
    private final boolean deliverToOthers;

    public DeliverAi(ClientGameState state, EntityReader carrier, EntityReader destination) {
        this(state, carrier, destination, false);
    }

    public DeliverAi(ClientGameState state, EntityReader carrier, EntityReader destination, boolean deliverToOthers) {
        super(state, carrier);
        this.destination = destination;
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

            if (Proximity.closeEnoughToInteract(controlling, destination)) {
                ar.setUnitActionToDeposit(controlling, destination, entry.getKey(), Integer.MAX_VALUE);
            } else {
                ar.setUnitActionToMove(controlling, destination);
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
