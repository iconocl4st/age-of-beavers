package client.ai;

import client.state.ClientGameState;
import common.Proximity;
import common.algo.AStar;
import common.state.EntityReader;
import common.state.spec.EntitySpec;
import common.state.spec.ResourceType;
import common.state.sst.sub.Load;
import common.util.GridLocationQuerier;

public class Gather extends Ai {

    private final EntitySpec naturalResourceSpec;
    private EntityReader currentTarget;
    private GatherState currentState;

    private enum GatherState {
        Delivering,
        Gathering,
    }

    public Gather(ClientGameState state, EntityReader gatherer, EntityReader target, EntitySpec resourceType) {
        super(state, gatherer);
        this.currentTarget = target;
        this.naturalResourceSpec = resourceType;
        currentState = GatherState.Delivering;
    }

    private ResourceType getResourceType() {
        // TODO: what if there are more than one? (like seeds...)
        return naturalResourceSpec.carrying.iterator().next().type;
    }

    public String toString() {
        return "gather " + naturalResourceSpec.name;
    }

    @Override
    public AiAttemptResult setActions(ActionRequester ar) {
        while (true) {
            AiAttemptResult deliveryAttempt = dropOffOtherResources(ar, getResourceType());
            if (!deliveryAttempt.equals(AiAttemptResult.NothingDone)) {
                return deliveryAttempt;
            }

            Load load = controlling.getCarrying();
            EntitySpec type = controlling.getType();
            if (anyAreNull(load, type))
                return AiAttemptResult.Unsuccessful;

            if (!controlling.canAccept(getResourceType())) {
                currentState = GatherState.Delivering;
            }

            if (load.getWeight() == 0)
                currentState = GatherState.Gathering;

            if (currentState.equals(GatherState.Delivering)) {
                return deliverToNearestDropOff(ar, load.getNonzeroResource(), controlling.entityId);
            }

            if (currentTarget != null) {
                ResourceType resourceType = currentTarget.getCarrying().getNonzeroResource();
                if (resourceType == null) {
                    currentTarget = null;
                    continue;
                }
                if (Proximity.closeEnoughToInteract(controlling, currentTarget)) {
                    ar.setUnitActionToCollect(controlling, currentTarget, resourceType);
                    return AiAttemptResult.Successful;
                }

                AStar.PathSearch results = GridLocationQuerier.findPath(context.gameState, controlling.entityId, currentTarget.entityId, context.currentPlayer);
                if (results == null) {
                    currentTarget = null;
                    continue;
                }
                ar.setUnitActionToMove(controlling, results.path);
                return AiAttemptResult.Successful;
            }

            GridLocationQuerier.NearestEntityQueryResults results = findNearestResource(naturalResourceSpec.name);
            if (!results.successful()) {
                return AiAttemptResult.Unsuccessful; // or successful?
            }
            currentTarget = results.getEntity(context.gameState);
        }
    }
}
