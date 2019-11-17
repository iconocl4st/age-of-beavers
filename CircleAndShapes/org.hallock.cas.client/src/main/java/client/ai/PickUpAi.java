package client.ai;

import client.app.ClientContext;
import common.Proximity;
import common.state.spec.ResourceType;
import common.state.EntityId;
import common.state.EntityReader;

import java.util.Arrays;
import java.util.HashSet;

public class PickUpAi extends Ai {

    private final EntityReader destination;
    private final ResourceType resource;


    public PickUpAi(ClientContext gameState, EntityId worker, EntityId destination, ResourceType resource) {
        super(gameState, worker);
        this.destination = new EntityReader(gameState.gameState, destination);
        this.resource = resource;
    }


    public String toString() {
        return "pickup " + resource;
    }

    @Override
    public AiAttemptResult setActions(ActionRequester ar) {
        if (!controlling.getType().containsAnyClass(new HashSet<>(Arrays.asList("storage", "carrier")))) {
            return AiAttemptResult.Unsuccessful;
        }

        AiAttemptResult deliveryAttempt = dropOffOtherResources(ar, resource);
        if (!deliveryAttempt.equals(AiAttemptResult.NothingDone)) {
            return deliveryAttempt;
        }

        if (destination.doesNotHave(resource)) {
            return AiAttemptResult.Completed;
        }

        if (!controlling.canAccept(resource)) {
            return AiAttemptResult.Completed;
        }

        if (Proximity.closeEnoughToInteract(context.gameState, controlling.entityId, destination.entityId)) {
            ar.setUnitActionToCollect(controlling, destination.entityId, resource, Integer.MAX_VALUE);
            return AiAttemptResult.Successful;
        } else {
            return ar.setUnitActionToMove(controlling, destination.entityId);
        }
    }
}
