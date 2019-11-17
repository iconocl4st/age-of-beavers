package client.ai;

import client.app.ClientContext;
import common.state.spec.CreationSpec;
import common.state.spec.EntitySpec;
import common.state.spec.ResourceType;
import common.state.EntityId;
import common.state.EntityReader;
import common.util.DPoint;

import java.util.HashMap;

public class AiManager {

    private final ClientContext context;
    private final HashMap<EntityId, Ai> currentAis = new HashMap<>();

    public AiManager(ClientContext context) {
        this.context = context;
    }

    public void setAi(EntityId entity, Ai ai) {
        removeAi(entity);
        context.eventManager.listenForEventsFrom(ai, entity);
        currentAis.put(entity, ai);
    }

    public void removeAi(EntityId unitId) {
        Ai ai = currentAis.remove(unitId);
        if (ai != null) {
            context.eventManager.stopListeningTo(ai, unitId);
        }
    }

    public Ai getCurrentAi(EntityId unit) {
        synchronized (currentAis) {
            return currentAis.get(unit);
        }
    }

    public void startAi(EntityId entity, Ai ai) {
        setAi(entity, ai);
        if (!ai.setActions(new ActionRequester(context)).equals(Ai.AiAttemptResult.Successful)) {
            removeAi(entity);
        }
    }


    public void setUnitAiToGather(EntityId gatherer, EntityId target, EntitySpec targetType) {
//        if (!c.gameState.playerManager.playerOwns(c.currentPlayer, gatherer)) return;
//        if (c.gameState.garrisonManager.isGarrisoned(gatherer)) return;

        // TODO
        // check if the unit exists
        // check if it is movable
        // hidden

        startAi(gatherer, new Gather(context, gatherer, target, targetType));
    }

    public void setUnitAiToHunt(EntityId hunter, EntityId prey, EntitySpec preyType) {
        // TODO, hidden?
        startAi(hunter, new HuntAi(context, hunter, prey, preyType));
    }

    public void setUnitAiToDeliver(EntityId deliverer, EntityId destination) {
//        if (!Zoom.serverState.state.playerManager.playerOwns(player, deliverer)) return;
//        if (Zoom.serverState.state.garrisonManager.isGarrisoned(deliverer)) return;

        startAi(deliverer, new DeliverAi(context, deliverer, destination));
    }

    public void setUnitAiToConstruct(EntityId constructor, EntityId constructionZone) {
//        if (!Zoom.serverState.state.playerManager.playerOwns(player, constructor)) return;
//        if (!Zoom.serverState.state.playerManager.playerOwns(player, constructionZone)) return;
//        if (Zoom.serverState.state.garrisonManager.isGarrisoned(constructor)) return;
        startAi(constructor, new ConstructAi(context, constructor, constructionZone));
    }

    public void setUnitAiToGarrison(EntityId toGarrison, EntityId garrisonIn) {
        startAi(toGarrison, new GarrisonAi(context, toGarrison, garrisonIn));
//        // todo: hidden
//        if (Zoom.serverState.state.garrisonManager.isGarrisoned(toGarrison)) return;
//        if (!GameStateHelper.playerCanGarrison(Zoom.serverState.state, player, toGarrison, ridden)) return;
    }


    // todo: dry
    public void setUnitAiToRide(EntityId rider, EntityId ridden) {
        startAi(rider, new RideAi(context, rider, ridden));
//            if (Zoom.serverState.state.hiddenManager.isHidden(rider)) return;
//            if (!GameStateHelper.playerCanRide(Zoom.serverState.state, player, rider, ridden)) return;
//            Ai ai = ;
//            Zoom.serverState.aiManager.setAi(rider, ai);
//            if (!ai.nextStep(this)) {
//                Zoom.serverState.aiManager.removeAi(rider);
//            }
    }

    public void setUnitAiToPickUp(EntityId worker, EntityId destination, ResourceType resourceType) {
        startAi(worker, new PickUpAi(context, worker, destination, resourceType));
//        Player owner = Zoom.serverState.state.playerManager.getOwner(destination);
//        if (!Zoom.serverState.state.playerManager.playerOwns(player, worker)) return;
//        if (!Zoom.serverState.state.playerManager.playerOwns(player, destination)) return;
//        if (Zoom.serverState.state.garrisonManager.isGarrisoned(worker)) return;
    }

    public void setUnitAiToRidden(EntityId currentlySelected, EntityId entity) {
        startAi(currentlySelected, new BeRidden(context, currentlySelected, entity));
    }

    public void setUnitAiToCreate(EntityReader currentlySelected, CreationSpec spec) {
        startAi(currentlySelected.entityId, new CreateAi(context, currentlySelected.entityId, spec));
    }

    public boolean isControlling(EntityReader entity) {
        return currentAis.containsKey(entity.entityId);
    }

    public void setUnitAiToMove(EntityReader entity, DPoint destination) {
        startAi(entity.entityId, new MoveAi(context, entity.entityId, destination));
    }
}
